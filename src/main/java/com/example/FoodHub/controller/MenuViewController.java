//package com.example.FoodHub.controller;
//
//import com.example.FoodHub.entity.Category;
//import com.example.FoodHub.repository.CategoryRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//@Controller
//public class MenuViewController {
//
//    @Autowired
//    private CategoryRepository categoryRepository;
//
//    @GetMapping("/menu")
//    public String showMenu(Model model,
//                           @RequestParam(required = false) Integer categoryId,
//                           @RequestParam(defaultValue = "name") String sortBy,
//                           @RequestParam(defaultValue = "asc") String sortDirection,
//                           @RequestParam(defaultValue = "0") int page,
//                           @RequestParam(defaultValue = "10") int size) {
//
//        model.addAttribute("categories", categoryRepository.findAll());
//        model.addAttribute("categoryId", categoryId);
//        model.addAttribute("sortBy", sortBy);
//        model.addAttribute("sortDirection", sortDirection);
//        model.addAttribute("page", page);
//        model.addAttribute("size", size);
//
//        return "menu";
//    }
//}
