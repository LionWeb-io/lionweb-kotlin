package com.strumenta.starlasulw

import io.lionweb.language.INamed
import io.lionweb.model.Node

interface NamedLW : Node {
    var name: String?
}