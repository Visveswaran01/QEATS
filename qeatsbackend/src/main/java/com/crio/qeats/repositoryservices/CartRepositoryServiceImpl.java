
/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositoryservices;

import com.crio.qeats.dto.Cart;
import com.crio.qeats.dto.Item;
import com.crio.qeats.exceptions.CartNotFoundException;
import com.crio.qeats.models.CartEntity;
import com.crio.qeats.repositories.CartRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.inject.Provider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class CartRepositoryServiceImpl implements CartRepositoryService {

  @Autowired
  private CartRepository cartRepository;
  
  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private Provider<ModelMapper> modelMapperProvider;

  @Override
  public String createCart(Cart cart) {
    // TODO Auto-generated method stub

    //mapping dao to entity class
    ModelMapper modelMapper = modelMapperProvider.get();
    CartEntity cartEntity = modelMapper.map(cart,CartEntity.class);
    
    //saving cart to mongoDB 
    cartEntity = cartRepository.save(cartEntity);
    return (cartEntity.getId());
  }

  @Override
  public Optional<Cart> findCartByUserId(String userId) {
    // TODO Auto-generated method stub
    
    //adding filter using addCriteria condition
    Query query = new Query();
    query.addCriteria(Criteria.where("userId").is(userId));
    
    //extract result from mongodb and map it view layer class
    CartEntity cartEntity = mongoTemplate.findOne(query, CartEntity.class);
    ModelMapper modelMapper = modelMapperProvider.get();
    Cart userCart = modelMapper.map(cartEntity,Cart.class);

    //the result of userCart may or may not be null
    Optional<Cart> optionalCart = Optional.ofNullable(userCart);
  
    return optionalCart;
  }

  @Override
  public Cart findCartByCartId(String cartId) throws CartNotFoundException {
    // TODO Auto-generated method stub
    Query query = new Query();
    query.addCriteria(Criteria.where("id").is(cartId));
    
    //extract result from mongodb and map it view layer class
    CartEntity cartEntity = mongoTemplate.findOne(query, CartEntity.class);
    ModelMapper modelMapper = modelMapperProvider.get();
    Cart cartById = modelMapper.map(cartEntity,Cart.class);

    return cartById;
  }

  @Override
  public Cart addItem(Item item, String cartId, String restaurantId) throws CartNotFoundException {
    // TODO Auto-generated method stub
    Query query = new Query();
    query.addCriteria(Criteria.where("id").is(cartId));
    
    //fetching document that we need to update
    CartEntity cartEntity = mongoTemplate.findOne(query, CartEntity.class);
    if (cartEntity == null) {
      throw new CartNotFoundException();
    }
    
    //adding item to user
    cartEntity.addItem(item);
    cartEntity.setRestaurantId(restaurantId);
    
    //save the update changes in mongoDB
    cartEntity = cartRepository.save(cartEntity);
    
    //mapping update document to view layer
    ModelMapper modelMapper = modelMapperProvider.get();
    Cart updatedCart = modelMapper.map(cartEntity,Cart.class);

    return updatedCart;
  }

  @Override
  public Cart removeItem(Item item, String cartId, String restaurantId) 
      throws CartNotFoundException {
    // TODO Auto-generated method stub
    Query query = new Query();
    query.addCriteria(Criteria.where("id").is(cartId));
    
    //fetching document that we need to update
    CartEntity cartEntity = mongoTemplate.findOne(query, CartEntity.class);
    if (cartEntity == null) {
      throw new CartNotFoundException();
    }
    
    //removing item to user
    cartEntity.removeItem(item);
    cartEntity.setRestaurantId("");
    
    //save the update changes in mongoDB
    cartEntity = mongoTemplate.save(cartEntity);
    
    //mapping update document to view layer
    ModelMapper modelMapper = modelMapperProvider.get();
    Cart updatedCart = modelMapper.map(cartEntity,Cart.class);

    return updatedCart;
  }

}