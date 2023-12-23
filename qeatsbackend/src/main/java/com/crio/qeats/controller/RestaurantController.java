/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.controller;

import com.crio.qeats.dto.Cart;
import com.crio.qeats.dto.Order;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.exceptions.ItemNotFromSameRestaurantException;
import com.crio.qeats.exchanges.AddCartRequest;
import com.crio.qeats.exchanges.CartModifiedResponse;
import com.crio.qeats.exchanges.DeleteCartRequest;
import com.crio.qeats.exchanges.GetCartRequest;
import com.crio.qeats.exchanges.GetMenuResponse;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.exchanges.PostOrderRequest;
import com.crio.qeats.services.CartAndOrderService;
import com.crio.qeats.services.MenuService;
import com.crio.qeats.services.RestaurantService;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



// Implement Controller using Spring annotations.
// Remember, annotations have various "targets". They can be class level, method level or others.
@RestController
@Log4j2
@RequestMapping(RestaurantController.RESTAURANT_API_ENDPOINT)
public class RestaurantController {

  public static final String RESTAURANT_API_ENDPOINT = "/qeats/v1";
  public static final String RESTAURANTS_API = "/restaurants";
  public static final String MENU_API = "/menu";
  public static final String CART_API = "/cart";
  public static final String CART_ITEM_API = "/cart/item";
  public static final String CART_CLEAR_API = "/cart/clear";
  public static final String POST_ORDER_API = "/order";
  public static final String GET_ORDERS_API = "/orders";

  @Autowired
  private RestaurantService restaurantService;

  @Autowired
  private CartAndOrderService cartAndOrderService;


  // TODO: CRIO_TASK_MODULE_MULTITHREADING
  //  Improve the performance of this GetRestaurants API
  //  and keep the functionality same.
  // Get the list of open restaurants near the specified latitude/longitude & matching searchFor.
  // API URI: /qeats/v1/restaurants?latitude=21.93&longitude=23.0&searchFor=tamil
  // Method: GET
  // Query Params: latitude, longitude, searchFor(optional)
  // Success Output:
  // 1). If searchFor param is present, return restaurants as a list matching the following criteria
  //   1) open now
  //   2) is near the specified latitude and longitude
  //   3) searchFor matching(partially or fully):
  //      - restaurant name
  //      - or restaurant attribute
  //      - or item name
  //      - or item attribute (all matching is done ignoring case)
  //
  //   4) order the list by following the rules before returning
  //      1) Restaurant name
  //          - exact matches first
  //          - partial matches second
  //      2) Restaurant attributes
  //          - partial and full matches in any order
  //      3) Item name
  //          - exact matches first
  //          - partial matches second
  //      4) Item attributes
  //          - partial and full matches in any order
  //      Eg: For example, when user searches for "Udupi", "Udupi Bhavan" restaurant should
  //      come ahead of restaurants having "Udupi" in attribute.
  // 2). If searchFor param is absent,
  //     1) If there are restaurants near by return the list
  //     2) Else return empty list
  //
  // - For peak hours: 8AM-10AM, 1PM-2PM, 7PM-9PM
  //   - service radius is 3KMs.
  // - All other times
  //   - serving radius is 5KMs.
  // - If there are no restaurants, return empty list of restaurants.
  //
  //
  // HTTP Code: 200
  // {
  //  "restaurants": [
  //    {
  //      "restaurantId": "10",
  //      "name": "A2B",
  //      "city": "Hsr Layout",
  //      "imageUrl": "www.google.com",
  //      "latitude": 20.027,
  //      "longitude": 30.0,
  //      "opensAt": "18:00",
  //      "closesAt": "23:00",
  //      "attributes": [
  //        "Tamil",
  //        "South Indian"
  //      ]
  //    }
  //  ]
  // }
  //
  @GetMapping(RESTAURANTS_API)
  public ResponseEntity<GetRestaurantsResponse> getRestaurants(
       GetRestaurantsRequest getRestaurantsRequest) {
    log.info("getRestaurants called with {}", getRestaurantsRequest);
    GetRestaurantsResponse getRestaurantsResponse;
    if (!(getRestaurantsRequest.getLatitude() != null 
          && getRestaurantsRequest.getLongitude() != null
          && getRestaurantsRequest.getLatitude() >= -90 && getRestaurantsRequest.getLatitude() <= 90
          && getRestaurantsRequest.getLongitude() >= -180 
          && getRestaurantsRequest.getLongitude() <= 180)) {

      return ResponseEntity.badRequest().body(null);

    }
     
    List<Restaurant> restaurants;

    if (StringUtils.isEmpty(getRestaurantsRequest.getSearchFor())) {
      getRestaurantsResponse = restaurantService
                            .findAllRestaurantsCloseBy(getRestaurantsRequest, LocalTime.now());
    } else {
      getRestaurantsResponse = restaurantService
                     .findRestaurantsBySearchQuery(getRestaurantsRequest, LocalTime.now());
      log.info("getRestaurants returned {}", getRestaurantsResponse);
    }

    //To avoid null pointer exception
    if (getRestaurantsResponse != null) {
      
      restaurants = getRestaurantsResponse.getRestaurants();
      for (int i = 0; i < restaurants.size(); i++) {
        restaurants.get(i).setName(restaurants.get(i).getName().replace("é", "?"));
      }
    }
      
    log.info("getRestaurants returned {}", getRestaurantsResponse);
    return ResponseEntity.ok().body(getRestaurantsResponse);

  }


  

  // TODO: CRIO_TASK_MODULE_MENUAPI
  // Implement GET Cart for the given userId.
  // API URI: /qeats/v1/cart?userId=arun
  // Method: GET
  // Query Params: userId
  // Success Output:
  // 1). If userId is present return user's cart
  //     - If user has an active cart, then return it
  //     - otherwise return an empty cart
  //
  // 2). If userId is not present then respond with BadHttpRequest.
  //
  // HTTP Code: 200
  // {
  //  "id": "10",
  //  "items": [
  //    {
  //      "attributes": [
  //        "South Indian"
  //      ],
  //      "id": "1",
  //      "imageUrl": "www.google.com",
  //      "itemId": "10",
  //      "name": "Idly",
  //      "price": 45
  //    }
  //  ],
  //  "restaurantId": "11",
  //  "total": 45,
  //  "userId": "arun"
  // }
  // Error Response:
  // HTTP Code: 4xx, if client side error.
  //          : 5xx, if server side error.
  // Eg:
  // curl -X GET "http://localhost:8081/qeats/v1/restaurants?latitude=28.4900591&longitude=77.536386&searchFor=tamil"

 
  // TIP(MODULE_MENUAPI): Model Implementation for getting menu given a restaurantId.
  // Get the Menu for the given restaurantId
  // API URI: /qeats/v1/menu?restaurantId=11
  // Method: GET
  // Query Params: restaurantId
  // Success Output:
  // 1). If restaurantId is present return Menu
  // 2). Otherwise respond with BadHttpRequest.
  //
  // HTTP Code: 200
  // {
  //  "menu": {
  // curl -X GET "http://localhost:8081/qeats/v1/cart?userId=arun"

  @GetMapping(CART_API)
  public ResponseEntity<Cart> getCart(GetCartRequest getCartRequest) {

    log.info("getCartRequest called with {}", getCartRequest);
    if (getCartRequest.getId() == null) {

      return ResponseEntity.badRequest().body(null);
    }

    Cart obtainedCart = cartAndOrderService.findOrCreateCart(getCartRequest.getId());
    log.info("getCartRequest returned {}", getCartRequest);
    return ResponseEntity.ok().body(obtainedCart);
  }


  // TODO: CRIO_TASK_MODULE_MENUAPI
  // Implement add item to cart
  // API URI: /qeats/v1/cart/item
  // Method: POST
  // Request Body format:
  //  {
  //    "cartId": "1",
  //    "itemId": "10",
  //    "restaurantId": "11"
  //  }
  //
  // Success Output:
  // 1). If user has an active cart, add item to the cart.
  // 2). Otherwise create an empty cart and add the given item.
  // 3). If item to be added is not from same restaurant the 'cartResponseType' should be
  //     QEatsException.ITEM_NOT_FOUND_IN_RESTAURANT_MENU.
  //
  // HTTP Code: 200
  // Response body contains
  //  {
  //    "cart": {
  //      "id": "1",
  //      "items": [
  //        {
  //          "attributes": [
  //            "South Indian"
  //          ],
  //          "id": "1",
  //          "imageUrl": "www.google.com",
  //          "itemId": "10",
  //          "name": "Idly",
  //          "price": 45
  //        }
  //      ],
  //      "restaurantId": "11",
  //      "total": 45,
  //      "userId": "arun"
  //     },
  //     "cartResponseType": 0
  //  }
  // Error Response:
  // HTTP Code: 4xx, if client side error.
  //          : 5xx, if server side error.
  // curl -X GET "http://localhost:8081/qeats/v1/cart/item"

  @PostMapping(CART_ITEM_API)
  public ResponseEntity<CartModifiedResponse> addItem(AddCartRequest addCartRequest) {

    CartModifiedResponse cartResponse;
    Cart cart = cartAndOrderService.findOrCreateCart(addCartRequest.getCartId());
    
    String itemId = addCartRequest.getItemId();
    String cartId = addCartRequest.getCartId();
    String restaurantId = addCartRequest.getRestaurantId();
    cartResponse = cartAndOrderService.addItemToCart(itemId, cartId, restaurantId);
    return ResponseEntity.ok().body(cartResponse);
  }


  // TODO: CRIO_TASK_MODULE_MENUAPI
  // Implement remove item from given cartId
  // API URI: /qeats/v1/cart/item
  // Method: DELETE
  // Request Body format:
  //  {
  //    "cartId": "1",
  //    "itemId": "10",
  //    "restaurantId": "11"
  //  }
  //
  // Success Output:
  // 1). If item is present in user cart, then remove it.
  // 2). Otherwise, do nothing.
  //
  // HTTP Code: 200
  // Response body contains
  //  {
  //    "cart" : {
  //      "id": "1",
  //      "items": [ ],
  //      "restaurantId": "",
  //      "total": 0,
  //      "userId": "arun"
  //     },
  //     "cartResponseType": 0
  //  }
  // Error Response:
  // HTTP Code: 4xx, if client side error.
  //          : 5xx, if server side error.
  // curl -X GET "http://localhost:8081/qeats/v1/cart/item"
  
  @DeleteMapping(CART_ITEM_API)
  public ResponseEntity<CartModifiedResponse> deleteItem(DeleteCartRequest deleteCartRequest) {
    CartModifiedResponse cartResponse;
    
    log.info("deleteCartRequest called with {}", deleteCartRequest);
    cartAndOrderService.findOrCreateCart(deleteCartRequest.getCartId());
      
    String itemId = deleteCartRequest.getItemId();
    String cartId = deleteCartRequest.getCartId();
    String restaurantId = deleteCartRequest.getRestaurantId();
    cartResponse = cartAndOrderService.removeItemFromCart(itemId, cartId, restaurantId);
    cartResponse.setCartResponseType(0);
    
    log.info("deleteCartRequest returned {}", deleteCartRequest);
    return ResponseEntity.ok().body(cartResponse);
  }


  // TODO: CRIO_TASK_MODULE_MENUAPI
  // Clear cart for the given cartId.
  // API URI: /qeats/v1/cart/item
  // Method: POST
  // Request Body format:
  //  {
  //    "cartId": "1"
  //  }
  //
  // Success Output:
  // 1). If user has an active cart clear it
  // 2). If cartId is not present then cartResponseType should be 103
  // HTTP Code: 200
  // Response body contains
  //  {
  //    "cart": {
  //    "id":"10",
  //        "items": [
  //    {
  //      "attributes": [
  //      "South Indian"
  //            ],
  //      "id":"1",
  //        "imageUrl":"www.google.com",
  //        "itemId":"10",
  //        "name":"Idly",
  //        "price":45
  //    }
  //        ],
  //    "restaurantId":"11",
  //        "total":45,
  //        "userId":"arun"
  //    },
  //    "cartResponseType":0
  //  }
  // Error Response:
  // HTTP Code: 4xx, if client side error.
  //          : 5xx, if server side error.
  // curl -X GET "http://localhost:8081/qeats/v1/cart/item"
  //  @PutMapping(path = CART_CLEAR_API, consumes = "application/json")
  //  public ResponseEntity<CartModifiedResponse> clearCart(
  //      @RequestBody @Valid ClearCartRequest clearCartRequest) {
  //
  //    try {
  //      CartModifiedResponse cartModifiedResponse
  //          = cartAndOrderService.clearCart(clearCartRequest.getCartId());
  //      return ResponseEntity.ok().body(cartModifiedResponse);
  //    } catch (Exception e) {
  //      return ResponseEntity.badRequest().build();
  //    }
  //  }

  // TODO: CRIO_TASK_MODULE_MENUAPI
  // Place order for the given cartId.
  // API URI: /qeats/v1/order
  // Method: POST
  // Request Body format:
  //  {
  //    "cartId": "1"
  //  }
  //
  // Success Output:
  // 1). Place order for the given cartId and clear the cart.
  // 2). If cart is empty then response should be Bad Http Request.
  //
  // HTTP Code: 200
  // Response body contains
  //  {
  //    "id": "1",
  //    "items": [
  //      {
  //        "attributes": [
  //          "South Indian"
  //        ],
  //        "id": "1",
  //        "imageUrl": "www.google.com",
  //        "itemId": "10",
  //        "name": "Idly",
  //        "price": 45
  //      }
  //    ],
  //    "restaurantId": "11"
  //  }
  // }
  // Error Response:
  // HTTP Code: 4xx, if client side error.
  //          : 5xx, if server side error.
  // Eg:
  // curl -X GET "http://localhost:8081/qeats/v1/menu?restaurantId=11"

  //    "restaurantId": "11",
  //    "total": 45,
  //    "userId": "arun"
  //  }
  // Error Response:
  // HTTP Code: 4xx, if client side error.
  //          : 5xx, if server side error.
  // curl -X GET "http://localhost:8081/qeats/v1/order"
    
  @PostMapping(POST_ORDER_API)
  public ResponseEntity<Order> placeOrder(PostOrderRequest postOrderRequest) {

    Order placed = cartAndOrderService.postOrder(postOrderRequest.getCartId());
    if (placed == null) {
      return ResponseEntity.badRequest().body(null);
    }
    return ResponseEntity.ok().body(placed);
  }


  // TODO: CRIO_TASK_MODULE_MENUAPI
  // Implement GET list of orders for the given userId.
  // API URI: /qeats/v1/orders?userId=arun
  // Method: GET
  // Query Params: userId
  // Success Output:
  // 1). Return the list of orders for the given userId
  //     - return an empty list of none exists
  // 2). If userId is not present then return empty list of orders.//
  //  HTTP Code: 200
  //  [
  //    {
  //      "id": "1",
  //      "items": [
  //        {
  //          "attributes": [
  //            "South Indian"
  //          ],
  //          "id": "1",
  //          "imageUrl": "www.google.com",
  //          "itemId": "10",
  //          "name": "Idly",
  //          "price": 45
  //        }
  //      ],
  //      "restaurantId": "11",
  //      "timePlaced": "",
  //      "total": 45,
  //      "userId": "string"
  //    }
  //  ]
  // Error Response:
  // HTTP Code: 4xx, if client side error.
  //          : 5xx, if server side error.
  // Eg:
  // curl -X GET "http://localhost:8081/qeats/v1/orders?userId=arun"
  //  @GetMapping(GET_ORDERS_API)
  //  public ResponseEntity<List<Order>> getOrders(@Valid GetOrdersRequest getOrdersRequest) {
  //    return ResponseEntity.ok()
  //        .body(cartAndOrderService.getAllUserOrders(getOrdersRequest.getUserId()));
  //  }
}

