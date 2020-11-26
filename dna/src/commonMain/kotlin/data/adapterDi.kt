package com.github.trilobiitti.trilobite.dna.data

import com.github.trilobiitti.trilobite.dna.di.bindDependency
import kotlin.reflect.KProperty

object DocumentAdapters {
    val fieldAccessor = bindDependency<FieldAccessor<*>, KProperty<*>, Document>(
        "document adapter field accessor"
    )

    val fieldReader = bindDependency<FieldReader<*>, KProperty<*>, Document>(
        "document adapter field reader"
    ) { prop, doc -> fieldAccessor(prop, doc) }

    val fieldWriter = bindDependency<FieldReader<*>, KProperty<*>, Document>(
        "document adapter field writer"
    ) { prop, doc -> fieldAccessor(prop, doc) }
}
