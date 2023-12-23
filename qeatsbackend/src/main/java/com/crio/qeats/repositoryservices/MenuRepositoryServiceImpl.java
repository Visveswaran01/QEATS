/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositoryservices;

import com.crio.qeats.dto.Menu;
import com.crio.qeats.models.MenuEntity;
import com.crio.qeats.repositories.MenuRepository;
import java.util.Optional;
import javax.inject.Provider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MenuRepositoryServiceImpl implements MenuRepositoryService {

  @Autowired
  private MenuRepository menuRepository;

  @Autowired
  private Provider<ModelMapper> modelMapperProvider;

  @Override
  public Menu findMenu(String restaurantId) {
    // TODO Auto-generated method stub 
    MenuEntity menuEntity;
    Menu menu = null;

    Optional<MenuEntity> optionalMenuEntity = menuRepository.findMenuByRestaurantId(restaurantId);
    
    if (optionalMenuEntity.isPresent()) {
      menuEntity = optionalMenuEntity.get();
      ModelMapper modelMapper = modelMapperProvider.get();
      menu = modelMapper.map(menuEntity, Menu.class);
    }

    return menu;
  }

}
