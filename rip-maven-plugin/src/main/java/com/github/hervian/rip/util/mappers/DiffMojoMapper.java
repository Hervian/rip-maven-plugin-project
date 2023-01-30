package com.github.hervian.rip.util.mappers;

import com.github.hervian.rip.DiffMojo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DiffMojoMapper extends MojoMapper<DiffMojo> {

  DiffMojoMapper INSTANCE = Mappers.getMapper(DiffMojoMapper.class);

}
