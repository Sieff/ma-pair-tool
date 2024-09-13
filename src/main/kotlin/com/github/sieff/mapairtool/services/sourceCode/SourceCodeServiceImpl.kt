package com.github.sieff.mapairtool.services.sourceCode

import com.github.sieff.mapairtool.model.sourceCode.SourceCodeFile
import com.github.sieff.mapairtool.model.sourceCode.SourceCodeLine
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiRecursiveElementVisitor


class SourceCodeServiceImpl(val project: Project): SourceCodeService {
    override fun getActiveFile(): SourceCodeFile? {
        val activeFile = getActiveVirtualFile()
        if (activeFile != null) {
            return SourceCodeFile(
                activeFile.name,
                activeFile.readText().split("\n").mapIndexed {index: Int, codeLine: String -> SourceCodeLine(index + 1, codeLine) }
            )
        }
        return null
    }

    override fun getActiveFileReferences(): List<SourceCodeFile> {
        val contents: MutableList<SourceCodeFile> = ArrayList()

        for (referencedPsiFile in getActivePsiFileReferences()) {
            val content = ReadAction.compute<SourceCodeFile, Throwable> {
                SourceCodeFile(
                    referencedPsiFile.name,
                    referencedPsiFile.text.split("\n").mapIndexed {index: Int, codeLine: String -> SourceCodeLine(index + 1, codeLine) }
                )
            }
            contents.add(content)
        }

        return contents.toSet().toList()
    }

    override fun getOpenFiles(): List<SourceCodeFile> {
        return getOpenVirtualFiles()
            .map { SourceCodeFile(
                it.name,
                it.readText().split("\n").mapIndexed {index: Int, codeLine: String -> SourceCodeLine(index + 1, codeLine) }
            )
            }
            .toSet().toList()
    }

    private fun isFileInProject(psiFile: PsiFile): Boolean {
        val fileIndex = ProjectRootManager.getInstance(project).fileIndex
        return ReadAction.compute<Boolean, Throwable> {
            fileIndex.isInContent(psiFile.virtualFile)
        }
    }

    private fun getActivePsiFile(): PsiFile? {
        val currentFile = getActiveVirtualFile()

        if (currentFile != null) {
            return ReadAction.compute<PsiFile, Throwable> {
                PsiManager.getInstance(project).findFile(currentFile)
            }
        }

        return null
    }

    private fun getActivePsiFileReferences(): List<PsiFile> {
        if (getActivePsiFile() != null) {
            return getReferences(getActivePsiFile()!!).filter { isFileInProject(it) }
        }

        return ArrayList()
    }

    private fun getActiveVirtualFile(): VirtualFile? {
        val activeVirtualFiles = getSelectedVirtualFiles()

        return if (activeVirtualFiles.isNotEmpty()) {
            activeVirtualFiles[0]
        } else {
            null
        }
    }

    private fun getSelectedVirtualFiles(): List<VirtualFile> {
        val fileEditorManager = FileEditorManager.getInstance(project)
        return fileEditorManager.selectedFiles.toList()
    }

    private fun getOpenVirtualFiles(): List<VirtualFile> {
        val fileEditorManager = FileEditorManager.getInstance(project)
        return fileEditorManager.openFiles.toList()
    }

    private fun getReferences(psiFile: PsiFile): List<PsiFile> {
        val referencedFiles: MutableList<PsiFile> = ArrayList()

        return ReadAction.compute<List<PsiFile>, Throwable> {
            psiFile.accept(object : PsiRecursiveElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    super.visitElement(element)

                    for (reference in element.references) {
                        val resolvedElement = reference.resolve()
                        if (resolvedElement != null) {
                            val resolvedPsiFile = resolvedElement.containingFile
                            if (resolvedPsiFile != null) {
                                referencedFiles.add(resolvedPsiFile)
                            }
                        }
                    }
                }
            })

            referencedFiles
        }
    }
}