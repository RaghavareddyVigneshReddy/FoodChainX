package com.cts.FoodChainX.dto.farm;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FarmRequestDto{
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Location is required")
    private String location;

} 
