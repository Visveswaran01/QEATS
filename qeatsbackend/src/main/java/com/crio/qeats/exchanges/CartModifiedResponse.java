package com.crio.qeats.exchanges;

import com.crio.qeats.dto.Cart;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.net.http.HttpResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@Data
@NoArgsConstructor
public class CartModifiedResponse {

  Cart cart;
  int  cartResponseType;
}
