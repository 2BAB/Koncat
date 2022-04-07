package me.xx2bab.koncat.compiler

import me.xx2bab.koncat.contract.KoncatArgumentsContract
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class KoncatGenerationExtension(contract: KoncatArgumentsContract): IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        TODO("Not yet implemented")
    }
}