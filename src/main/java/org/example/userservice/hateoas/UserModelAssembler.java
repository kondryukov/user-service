package org.example.userservice.hateoas;

import org.example.userservice.controller.UserController;
import org.example.userservice.dto.UserResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class UserModelAssembler implements RepresentationModelAssembler<UserResponse, EntityModel<UserResponse>> {

    @Override
    public EntityModel<UserResponse> toModel(UserResponse user) {
        Link selfRelation = linkTo(methodOn(UserController.class).getUserById(user.id())).withSelfRel();
        Link allUsers = linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users");
        Link updateUser = linkTo(methodOn(UserController.class).updateUser(user.id(), null)).withRel("update");
        Link deleteUser = linkTo(methodOn(UserController.class).deleteUser(user.id())).withRel("delete");
        return EntityModel.of(user, selfRelation, allUsers, updateUser, deleteUser);
    }

    public CollectionModel<EntityModel<UserResponse>> toCollectionModel(List<UserResponse> users) {
        List<EntityModel<UserResponse>> models = new ArrayList<>();
        for (UserResponse user : users) {
            EntityModel<UserResponse> model = toModel(user);
            models.add(model);
        }
        Link selfRelation = linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel();
        Link createLink = linkTo(methodOn(UserController.class).createUser(null, null)).withRel("create");
        return CollectionModel.of(models, selfRelation, createLink);
    }
}
