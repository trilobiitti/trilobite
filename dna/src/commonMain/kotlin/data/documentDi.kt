package com.github.trilobiitti.trilobite.dna.data

import com.github.trilobiitti.trilobite.dna.di.bindDependency

val fieldKeyFromString = bindDependency<DocumentFieldKey, String>("field key from string") { str ->
    DocumentFieldKey(str)
}

val newEmptyDocument = bindDependency<Document>("new empty document") { MapDocument() }

