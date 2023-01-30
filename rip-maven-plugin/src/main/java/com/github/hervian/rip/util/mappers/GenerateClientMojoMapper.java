package com.github.hervian.rip.util.mappers;

import com.github.hervian.rip.GenerateClientMojo;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GenerateClientMojoMapper extends MojoMapper<GenerateClientMojo> {

  GenerateClientMojoMapper INSTANCE = Mappers.getMapper(GenerateClientMojoMapper.class);

}
