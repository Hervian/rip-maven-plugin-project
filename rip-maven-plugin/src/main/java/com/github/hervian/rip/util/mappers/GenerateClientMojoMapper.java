package com.github.hervian.rip.util.mappers;

import com.github.hervian.rip.GenerateClientMojo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GenerateClientMojoMapper extends MojoMapper<GenerateClientMojo> {

  GenerateClientMojoMapper INSTANCE = Mappers.getMapper(GenerateClientMojoMapper.class);

}
