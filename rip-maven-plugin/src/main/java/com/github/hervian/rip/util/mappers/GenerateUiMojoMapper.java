package com.github.hervian.rip.util.mappers;

import com.github.hervian.rip.GenerateUiMojo;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GenerateUiMojoMapper extends MojoMapper<GenerateUiMojo> {

  GenerateUiMojoMapper INSTANCE = Mappers.getMapper(GenerateUiMojoMapper.class);

}
