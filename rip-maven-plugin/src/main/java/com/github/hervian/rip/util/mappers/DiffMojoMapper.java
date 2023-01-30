package com.github.hervian.rip.util.mappers;

import com.github.hervian.rip.DiffMojo;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DiffMojoMapper extends MojoMapper<DiffMojo> {

  DiffMojoMapper INSTANCE = Mappers.getMapper(DiffMojoMapper.class);

}
