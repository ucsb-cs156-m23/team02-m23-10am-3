package edu.ucsb.cs156.example.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenu;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
// import lombok.extern.slf4j.Slf4j;

@Tag(name = "UCSBDiningCommonsMenu")
@RequestMapping("/api/ucsbdiningcommonsmenu")
@RestController
// @Slf4j
public class UCSBDiningCommonsMenuController extends ApiController {

    @Autowired
    UCSBDiningCommonsMenuRepository ucsbDiningCommonsMenuRepository;

    @Operation(summary= "Menu for the UCSB dining commons, Carrillo, De La Guerra, Ortega, Portola.")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")

    public Iterable<UCSBDiningCommonsMenu> getMenu() {
        Iterable<UCSBDiningCommonsMenu> menu = ucsbDiningCommonsMenuRepository.findAll();
        return menu;
    }

    @Operation(summary= "Get a single DiningCommonMenu item")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBDiningCommonsMenu getById(
            @Parameter(name="id") @RequestParam Long id) {
        UCSBDiningCommonsMenu commons = ucsbDiningCommonsMenuRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenu.class, id));

        return commons;
    }

    @Operation(summary= "Create a new UCSBDiningCommonsMenu item")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBDiningCommonsMenu postMenu(
        @Parameter(name="id") @RequestParam Long id,
        @Parameter(name="diningCommonsCode") @RequestParam String diningCommonsCode, 
        @Parameter(name="name") @RequestParam String name,
        @Parameter(name="station") @RequestParam String station
        )
        {

        UCSBDiningCommonsMenu menu = new UCSBDiningCommonsMenu();
        menu.setDiningCommonsCode(diningCommonsCode);
        menu.setName(name);
        menu.setStation(station);

        UCSBDiningCommonsMenu savedMenu = ucsbDiningCommonsMenuRepository.save(menu);

        return savedMenu;
    }

    @Operation(summary= "Delete a UCSBDiningCommonsMenu item")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteMenuItem(
            @Parameter(name="id") @RequestParam Long id) {
        UCSBDiningCommonsMenu menu = ucsbDiningCommonsMenuRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenu.class, id));

        ucsbDiningCommonsMenuRepository.delete(menu);
        return genericMessage("UCSBDiningCommonsMenu with id %s deleted".formatted(id));
    }

    @Operation(summary= "Update a single UCSBDiningCommonsMenu item")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBDiningCommonsMenu updateCommons(
            @Parameter(name="id") @RequestParam Long id,
            @RequestBody @Valid UCSBDiningCommonsMenu incoming) {

        UCSBDiningCommonsMenu menu = ucsbDiningCommonsMenuRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenu.class, id));


        menu.setDiningCommonsCode(incoming.getDiningCommonsCode());  
        menu.setName(incoming.getName());
        menu.setStation(incoming.getStation());

        ucsbDiningCommonsMenuRepository.save(menu);

        return menu;
    }
}
