package org.obl.brisola.free

sealed trait HTTPMethod

case object GET extends HTTPMethod
case object POST extends HTTPMethod
case object PUT extends HTTPMethod
case object DELETE extends HTTPMethod
