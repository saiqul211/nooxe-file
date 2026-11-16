package com.nooxe.file.data

sealed class FsNode {
    data class Folder(
        val id: String,
        val name: String,
        val children: MutableList<FsNode> = mutableListOf()
    ) : FsNode()

    data class File(
        val id: String,
        val name: String,
        val kind: String,
        val sizeMB: Int,
        val added: String,
        val addedDays: Int
    ) : FsNode()
}

object StorageState {
    const val TOTAL_MB = 180 * 1024
    var usedMB: Int = 80 * 1024
    fun formatSize(mb: Int): String {
        return if (mb >= 1024) {
            val gb = mb / 1024.0
            if (gb >= 10) String.format("%.0f GB", gb) else String.format("%.1f GB", gb)
        } else "$mb MB"
    }
}

object FileSystem {

    val internalRoot: FsNode.Folder = FsNode.Folder("internal", "Internal Storage", mutableListOf(
        FsNode.Folder("my-storage", "My Storage", mutableListOf(
            FsNode.Folder("figma", "Figma files", mutableListOf(
                FsNode.File("f1", "Smartapp.fig", "fig", 100, "1 days ago", 1),
                FsNode.File("f2", "Smart Home Landing.fig", "fig", 90, "2 days ago", 2),
                FsNode.File("f3", "Ecommerce App.fig", "fig", 30, "4 days ago", 4),
                FsNode.File("f4", "Plants Green Web.fig", "fig", 1024, "5 April 2022", 500),
                FsNode.File("f5", "Digital Marketing App.fig", "fig", 1024, "2 April 2022", 505),
                FsNode.File("f6", "Portfolio landingpage.fig", "fig", 129, "2 April 2022", 505),
                FsNode.File("f7", "Portfolio landingpage.fig", "fig", 1024, "1 April 2022", 507)
            )),
            FsNode.Folder("design", "Design Assets", mutableListOf(
                FsNode.File("d1", "Brand book.pdf", "pdf", 24, "10 days ago", 10),
                FsNode.File("d2", "Logo pack.zip", "zip", 32, "12 days ago", 12)
            )),
            FsNode.Folder("shots", "Screenshots", mutableListOf(
                FsNode.File("s1", "Screenshot 2026-11-14.png", "png", 2, "1 days ago", 1)
            ))
        )),
        FsNode.Folder("documents", "Documents", mutableListOf(
            FsNode.File("doc1", "Q3 report.pdf", "pdf", 12, "3 days ago", 3),
            FsNode.File("doc2", "Invoice_0421.pdf", "pdf", 2, "6 days ago", 6),
            FsNode.File("doc3", "Contract.docx", "doc", 1, "8 days ago", 8)
        )),
        FsNode.Folder("pictures", "Pictures", mutableListOf(
            FsNode.File("p1", "Mountain view.png", "png", 8, "5 days ago", 5),
            FsNode.File("p2", "Team photo.png", "png", 6, "7 days ago", 7)
        )),
        FsNode.Folder("videos", "Videos", mutableListOf(
            FsNode.File("v1", "Intro.mp4", "mp4", 240, "9 days ago", 9)
        )),
        FsNode.Folder("audio", "Audio", mutableListOf(
            FsNode.File("a1", "Podcast ep12.mp3", "mp3", 48, "11 days ago", 11)
        )),
        FsNode.Folder("downloads", "Downloads", mutableListOf())
    ))

    val externalRoot: FsNode.Folder = FsNode.Folder("external", "External Storage", mutableListOf(
        FsNode.Folder("backup", "Backup", mutableListOf(
            FsNode.File("b1", "backup_2026.zip", "zip", 320, "15 days ago", 15)
        )),
        FsNode.Folder("music", "Music", mutableListOf(
            FsNode.File("m1", "playlist.m3u", "mp3", 1, "20 days ago", 20)
        )),
        FsNode.Folder("movies", "Movies", mutableListOf(
            FsNode.File("mv1", "Documentary.mp4", "mp4", 1200, "30 days ago", 30)
        ))
    ))

    fun rootFor(tab: String): FsNode.Folder = if (tab == "internal") internalRoot else externalRoot

    fun resolve(tab: String, path: List<String>): FsNode.Folder {
        var cur: FsNode.Folder = rootFor(tab)
        for (id in path.drop(1)) {
            val next = cur.children.filterIsInstance<FsNode.Folder>().firstOrNull { it.id == id }
            if (next != null) cur = next else break
        }
        return cur
    }

    fun removeFile(tab: String, id: String): FsNode.File? {
        val root = rootFor(tab)
        var removed: FsNode.File? = null
        fun rec(node: FsNode.Folder) {
            val iter = node.children.iterator()
            while (iter.hasNext()) {
                val c = iter.next()
                if (c is FsNode.File && c.id == id) {
                    removed = c
                    iter.remove()
                    StorageState.usedMB = (StorageState.usedMB - c.sizeMB).coerceAtLeast(0)
                } else if (c is FsNode.Folder) {
                    rec(c)
                }
            }
        }
        rec(root)
        return removed
    }

    fun removeFiles(tab: String, ids: Set<String>): Int {
        var count = 0
        for (id in ids) {
            if (removeFile(tab, id) != null) count++
        }
        return count
    }

    fun addFolder(tab: String, parentId: String, name: String): FsNode.Folder? {
        val parent = findFolder(tab, parentId) ?: return null
        val newFolder = FsNode.Folder("fld_${System.currentTimeMillis()}", name, mutableListOf())
        parent.children.add(0, newFolder)
        return newFolder
    }

    fun findFolder(tab: String, id: String): FsNode.Folder? {
        val root = rootFor(tab)
        if (root.id == id) return root
        fun rec(node: FsNode.Folder): FsNode.Folder? {
            for (c in node.children) {
                if (c is FsNode.Folder) {
                    if (c.id == id) return c
                    val r = rec(c)
                    if (r != null) return r
                }
            }
            return null
        }
        return rec(root)
    }

    fun totalSize(folder: FsNode.Folder): Int =
        folder.children.sumOf { if (it is FsNode.File) it.sizeMB else totalSize(it as FsNode.Folder) }

    fun recentFiles(tab: String): List<FsNode.File> {
        val root = rootFor(tab)
        val all = mutableListOf<FsNode.File>()
        fun rec(node: FsNode.Folder) {
            for (c in node.children) {
                if (c is FsNode.File) all.add(c)
                else if (c is FsNode.Folder) rec(c)
            }
        }
        rec(root)
        return all.sortedBy { it.addedDays }.take(3)
    }

    fun clearOldFiles(tab: String): Int {
        val root = rootFor(tab)
        val toDelete = mutableListOf<String>()
        fun rec(node: FsNode.Folder) {
            for (c in node.children) {
                if (c is FsNode.File && c.addedDays > 30) toDelete.add(c.id)
                else if (c is FsNode.Folder) rec(c)
            }
        }
        rec(root)
        return removeFiles(tab, toDelete.toSet())
    }
}

data class Category(
    val name: String,
    val iconRes: Int,
    val sizeLabel: String,
    val folderId: String,
    val chipColor: Int
)
