package com.example.commerce.inventory

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

//@RestController
//@RequestMapping("/inventories")
//class inventoryController (private val inventoryClient: com.example.commerce.InventoryClient){
//
//    @GetMapping("/{productId}")
//    fun getProductStock(@PathVariable productId: Int): Int? {
//        return inventoryClient.fetchProductStocks(productId)
//    }
//}