// Copyright 2000-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.idea.devkit.inspections.quickfix;

import com.intellij.CommonBundle;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.devkit.DevKitBundle;

import java.util.Collections;

abstract class BaseFix implements LocalQuickFix {
  protected final SmartPsiElementPointer<? extends PsiElement> myPointer;
  protected final boolean myOnTheFly;

  protected BaseFix(@NotNull PsiElement psiElement, boolean onTheFly) {
    myPointer = SmartPointerManager.createPointer(psiElement);
    myOnTheFly = onTheFly;
  }

  @Override
  public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project, @NotNull ProblemDescriptor previewDescriptor) {
    return IntentionPreviewInfo.EMPTY;
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    // can happen during batch-inspection if resolution has already been applied
    // to plugin.xml or java class
    PsiElement element = myPointer.getElement();
    if (element == null || !element.isValid()) return;

    boolean external = descriptor.getPsiElement().getContainingFile() != element.getContainingFile();
    if (external) {
      PsiClass clazz = PsiTreeUtil.getParentOfType(element, PsiClass.class, false);
      ReadonlyStatusHandler readonlyStatusHandler = ReadonlyStatusHandler.getInstance(project);
      ReadonlyStatusHandler.OperationStatus status = readonlyStatusHandler.ensureFilesWritable(Collections.singletonList(element.getContainingFile().getVirtualFile()));

      if (status.hasReadonlyFiles()) {
        String className = clazz != null ? clazz.getQualifiedName() : element.getContainingFile().getName();

        Messages.showErrorDialog(project,
                                   DevKitBundle.message("inspections.registration.problems.quickfix.read-only",
                                                        className),
                                   CommonBundle.getErrorTitle());
        return;
      }
    }

    try {
      doFix(project, descriptor, external);
    }
    catch (IncorrectOperationException e) {
      Logger.getInstance(getClass()).error(e);
    }
  }

  protected abstract void doFix(Project project, ProblemDescriptor descriptor, boolean external) throws IncorrectOperationException;
}
