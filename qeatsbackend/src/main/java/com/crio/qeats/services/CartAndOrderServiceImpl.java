
package com.crio.qeats.services;

import com.crio.qeats.dto.Cart;
import com.crio.qeats.dto.Item;
import com.crio.qeats.dto.Order;
import com.crio.qeats.exceptions.EmptyCartException;
import com.crio.qeats.exceptions.ItemNotFromSameRestaurantException;
import com.crio.qeats.exchanges.CartModifiedResponse;
import com.crio.qeats.globals.GlobalConstants;
import com.crio.qeats.repositoryservices.CartRepositoryService;
import com.crio.qeats.repositoryservices.OrderRepositoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartAndOrderServiceImpl implements CartAndOrderService {

  @Autowired
  private CartRepositoryService cartRepositoryService;

  @Autowired
  private OrderRepositoryService orderRepositoryService;

  @Autowired
  private MenuService menuService;



  @Override
  public Order postOrder(String cartId) throws EmptyCartException {
    Cart cart = cartRepositoryService.findCartByCartId(cartId);
    Order placedOrder = orderRepositoryService.placeOrder(cart);
    return placedOrder;
  }

  public Cart createAnEmptyCart(String userId) {
    Cart emptyCart = new Cart();
    emptyCart.setUserId(userId);
    emptyCart.setRestaurantId("");
    emptyCart.setTotal(0);
    return emptyCart;
  }

  
  @Override
  public Cart findOrCreateCart(String userId) {
    // TODO Auto-generated method stub
    Cart cart;
    //checks cart is already exist for the user
    Optional<Cart> cartAvailablity = cartRepositoryService.findCartByUserId(userId);
    if (cartAvailablity.isPresent()) {
      cart = cartAvailablity.get();
    } else {
      //creating empty cart of view layer
      cart = createAnEmptyCart(userId);
      
      //saving empty cart created to mongodb
      String id = cartRepositoryService.createCart(cart);

      //finds the created empty cart based on the cartId returned
      cart = cartRepositoryService.findCartByCartId(id);
    } 
    return cart;
  }

  @Override
  public CartModifiedResponse addItemToCart(String itemId, String cartId, String restaurantId)
      throws ItemNotFromSameRestaurantException {
    // TODO Auto-generated method stub
    CartModifiedResponse cartModifiedResponse = new CartModifiedResponse();
    
    //extract item present in restaurant by item id
    Item item = menuService.findItem(itemId, restaurantId);

    Cart cart = cartRepositoryService.findCartByCartId(cartId);

    if (!cart.getRestaurantId().equals(restaurantId)) {

      cartModifiedResponse.setCart(null);
      ItemNotFromSameRestaurantException e = 
           new ItemNotFromSameRestaurantException("Item not from same restaurant");
      
      cartModifiedResponse.setCartResponseType(e.getErrorType());
    } else {

      //addding item to cart
      cart = cartRepositoryService.addItem(item, cartId, restaurantId);
      cartModifiedResponse.setCart(cart);
      cartModifiedResponse.setCartResponseType(0);
    }
    
    
    
    return cartModifiedResponse;
  }

  @Override
  public CartModifiedResponse removeItemFromCart(String itemId, String cartId, String restaurantId
  ) {
    // TODO Auto-generated method stub
    Cart cart = null;
    CartModifiedResponse cartModifiedResponse = new CartModifiedResponse();
    
    try {
      //extract item present in restaurant by item id
      Item item = menuService.findItem(itemId, restaurantId);
      cartModifiedResponse.setCart(cart);
    
      //addding item to cart
      cart = cartRepositoryService.removeItem(item, cartId, restaurantId);
    } catch (ItemNotFromSameRestaurantException e) {
      //TODO: handle exception
      e.printStackTrace();
    }

    return cartModifiedResponse;
  }

}

