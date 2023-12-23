
/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositoryservices;

import com.crio.qeats.dto.Cart;
import com.crio.qeats.dto.Order;
import com.crio.qeats.exceptions.EmptyCartException;
import com.crio.qeats.models.CartEntity;
import com.crio.qeats.models.OrderEntity;
import com.crio.qeats.repositories.OrderRepository;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Provider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderRepositoryServiceImpl implements OrderRepositoryService {

  @Autowired
  private OrderRepository orderRepository;
  @Autowired
  private Provider<ModelMapper> modelMapperProvider;

  @Override
  public Order placeOrder(Cart cart) {
    // TODO Auto-generated method stub
    //mapping dao to entity class
    ModelMapper modelMapper = modelMapperProvider.get();
    OrderEntity orderEntity = modelMapper.map(cart,OrderEntity.class);
    
    //saving cart to mongoDB 
    orderEntity = orderRepository.save(orderEntity);

    ModelMapper modelMapper1 = modelMapperProvider.get();
    Order order = modelMapper1.map(orderEntity,Order.class);
    

    return order;
  }
}