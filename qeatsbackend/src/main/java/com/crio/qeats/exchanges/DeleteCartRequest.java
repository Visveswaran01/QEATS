package com.crio.qeats.exchanges;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@Data
@NoArgsConstructor
public class DeleteCartRequest {
  
  @NotNull
  private String cartId;

  @NotNull
  private String itemId;

  @NotNull
  private String restaurantId;
}

