package com.delivery.sopo.models.mapper

interface MapperInterface<T, R>
{
    fun entityToDto(entity:T):R
    fun dtoToEntity(dto:R):T
}