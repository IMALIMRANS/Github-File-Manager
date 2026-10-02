package com.example.data.model

data class FileTreeNode(
    val name: String,
    val path: String,
    val sha: String,
    val isDirectory: Boolean,
    val size: Long = 0L,
    val depth: Int = 0,
    val children: List<FileTreeNode> = emptyList(),
    val parentPath: String = ""
) {
    val isFile: Boolean get() = !isDirectory

    fun countTotalFiles(): Int {
        return if (isDirectory) {
            children.sumOf { it.countTotalFiles() }
        } else {
            1
        }
    }

    fun countTotalFolders(): Int {
        return if (isDirectory) {
            1 + children.sumOf { it.countTotalFolders() }
        } else {
            0
        }
    }

    fun toContentItem(): GitHubContentItem {
        return GitHubContentItem(
            name = name,
            path = path,
            sha = sha,
            size = size,
            url = "",
            htmlUrl = "",
            downloadUrl = null,
            type = if (isDirectory) "dir" else "file",
            content = null,
            encoding = null
        )
    }

    companion object {
        /**
         * Builds a recursive list of root FileTreeNodes from a flat list of GitHubTreeEntry or GitHubContentItem.
         */
        fun buildTreeFromEntries(entries: List<GitHubTreeEntry>): List<FileTreeNode> {
            val rootChildren = mutableListOf<FileTreeNode>()
            
            // Map to store directory nodes by their full path
            class NodeBuilder(
                val name: String,
                val path: String,
                val sha: String,
                val isDirectory: Boolean,
                val size: Long,
                val depth: Int,
                val parentPath: String
            ) {
                val children = mutableListOf<NodeBuilder>()

                fun build(): FileTreeNode {
                    val sortedChildren = children
                        .map { it.build() }
                        .sortedWith(compareBy<FileTreeNode> { !it.isDirectory }.thenBy { it.name.lowercase() })
                    
                    return FileTreeNode(
                        name = name,
                        path = path,
                        sha = sha,
                        isDirectory = isDirectory,
                        size = size,
                        depth = depth,
                        children = sortedChildren,
                        parentPath = parentPath
                    )
                }
            }

            val dirMap = mutableMapOf<String, NodeBuilder>()
            val rootBuilders = mutableListOf<NodeBuilder>()

            // Sort entries by path length / depth so parents are processed before children
            val sortedEntries = entries.sortedBy { it.path.count { ch -> ch == '/' } }

            for (entry in sortedEntries) {
                val path = entry.path.trim().removePrefix("/").removeSuffix("/")
                if (path.isEmpty()) continue

                val isDir = entry.isDirectory
                val name = path.substringAfterLast('/')
                val parentPath = if (path.contains('/')) path.substringBeforeLast('/') else ""
                val depth = if (path.contains('/')) path.count { it == '/' } else 0

                val node = NodeBuilder(
                    name = name,
                    path = path,
                    sha = entry.sha,
                    isDirectory = isDir,
                    size = entry.size,
                    depth = depth,
                    parentPath = parentPath
                )

                if (isDir) {
                    dirMap[path] = node
                }

                if (parentPath.isEmpty()) {
                    rootBuilders.add(node)
                } else {
                    val parentNode = dirMap[parentPath]
                    if (parentNode != null) {
                        parentNode.children.add(node)
                    } else {
                        // In case parent was missing from entries, add to root
                        rootBuilders.add(node)
                    }
                }
            }

            return rootBuilders
                .map { it.build() }
                .sortedWith(compareBy<FileTreeNode> { !it.isDirectory }.thenBy { it.name.lowercase() })
        }

        /**
         * Builds a tree from flat content items (e.g. from current directory listing or multiple merged directories)
         */
        fun fromContentItems(items: List<GitHubContentItem>, depth: Int = 0): List<FileTreeNode> {
            return items.map { item ->
                val parent = if (item.path.contains('/')) item.path.substringBeforeLast('/') else ""
                FileTreeNode(
                    name = item.name,
                    path = item.path,
                    sha = item.sha,
                    isDirectory = item.isDirectory,
                    size = item.size,
                    depth = depth,
                    children = emptyList(),
                    parentPath = parent
                )
            }.sortedWith(compareBy<FileTreeNode> { !it.isDirectory }.thenBy { it.name.lowercase() })
        }
    }
}
