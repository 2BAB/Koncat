package me.xx2bab.koncat.processor

import com.google.devtools.ksp.symbol.*
import me.xx2bab.koncat.api.KoncatProcMetadata

open class KSVisitorWithExportMetadata : KSVisitor<KoncatProcMetadata, Unit> {
    override fun visitAnnotated(annotated: KSAnnotated, data: KoncatProcMetadata) {
        
    }

    override fun visitAnnotation(annotation: KSAnnotation, data: KoncatProcMetadata) {
        
    }

    override fun visitCallableReference(reference: KSCallableReference, data: KoncatProcMetadata) {
        
    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: KoncatProcMetadata) {
        
    }

    override fun visitClassifierReference(reference: KSClassifierReference, data: KoncatProcMetadata) {
        
    }

    override fun visitDeclaration(declaration: KSDeclaration, data: KoncatProcMetadata) {
        
    }

    override fun visitDeclarationContainer(
        declarationContainer: KSDeclarationContainer,
        data: KoncatProcMetadata
    ) {
        
    }

    override fun visitDynamicReference(reference: KSDynamicReference, data: KoncatProcMetadata) {
        
    }

    override fun visitFile(file: KSFile, data: KoncatProcMetadata) {
        
    }

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: KoncatProcMetadata) {
        
    }

    override fun visitModifierListOwner(
        modifierListOwner: KSModifierListOwner,
        data: KoncatProcMetadata
    ) {
        
    }

    override fun visitNode(node: KSNode, data: KoncatProcMetadata) {
        
    }

    override fun visitParenthesizedReference(
        reference: KSParenthesizedReference,
        data: KoncatProcMetadata
    ) {
        
    }

    override fun visitPropertyAccessor(accessor: KSPropertyAccessor, data: KoncatProcMetadata) {
        
    }

    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: KoncatProcMetadata) {
//        
    }

    override fun visitPropertyGetter(getter: KSPropertyGetter, data: KoncatProcMetadata) {
        
    }

    override fun visitPropertySetter(setter: KSPropertySetter, data: KoncatProcMetadata) {
        
    }

    override fun visitReferenceElement(element: KSReferenceElement, data: KoncatProcMetadata) {
        
    }

    override fun visitTypeAlias(typeAlias: KSTypeAlias, data: KoncatProcMetadata) {
        
    }

    override fun visitTypeArgument(typeArgument: KSTypeArgument, data: KoncatProcMetadata) {
        
    }

    override fun visitTypeParameter(typeParameter: KSTypeParameter, data: KoncatProcMetadata) {
        
    }

    override fun visitTypeReference(typeReference: KSTypeReference, data: KoncatProcMetadata) {
        
    }

    override fun visitValueArgument(valueArgument: KSValueArgument, data: KoncatProcMetadata) {
        
    }

    override fun visitValueParameter(valueParameter: KSValueParameter, data: KoncatProcMetadata) {
        
    }


}
