package com.botmaker.lsp;

public interface ProblemReasons {
    final int NoError = 0;
    final int NotFound = 1;
    final int NotVisible = 2;
    final int Ambiguous = 3;
    final int InternalNameProvided = 4; // used if an internal name is used in source
    final int InheritedNameHidesEnclosingName = 5;
    final int NonStaticReferenceInConstructorInvocation = 6;
    final int NonStaticReferenceInStaticContext = 7;
    final int ReceiverTypeNotVisible = 8;
    final int IllegalSuperTypeVariable = 9;
    final int ParameterBoundMismatch = 10; // for generic method
    final int TypeParameterArityMismatch = 11; // for generic method
    final int ParameterizedMethodTypeMismatch = 12; // for generic method
    final int TypeArgumentsForRawGenericMethod = 13; // for generic method
    final int InvalidTypeForStaticImport = 14;
    final int InvalidTypeForAutoManagedResource = 15;
    final int VarargsElementTypeNotVisible = 16;
    final int NoSuchSingleAbstractMethod = 17;
    final int NotAWellFormedParameterizedType = 18;
    // no longer in use: final int IntersectionHasMultipleFunctionalInterfaces = 19;
    final int NonStaticOrAlienTypeReceiver = 20;
    final int AttemptToBypassDirectSuper = 21; // super access within default method
    final int DefectiveContainerAnnotationType = 22;
    final int InvocationTypeInferenceFailure = 23;
    final int ApplicableMethodOverriddenByInapplicable = 24;
    final int ContradictoryNullAnnotations = 25;
    final int NoSuchMethodOnArray = 26;
    final int InferredApplicableMethodInapplicable = 27; // 18.5.1 ignores arguments not pertinent to applicability. When these are taken into consideration method could fail applicability
    final int NoProperEnclosingInstance = 28;
    final int InterfaceMethodInvocationNotBelow18 = 29;
    final int NotAccessible = 30; // JLS 6.6.1 - module aspects
    final int ErrorAlreadyReported = 31;
}