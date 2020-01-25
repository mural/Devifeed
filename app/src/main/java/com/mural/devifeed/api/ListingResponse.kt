package com.mural.devifeed.api

class ListingResponse(
    val children: List<ChildrenResponse>,
    val after: String?,
    val before: String?
)