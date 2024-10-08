package org.tkit.onecx.workspace.rs.user.mappers;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.text.StringSubstitutor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.onecx.workspace.domain.models.MenuItem;
import org.tkit.onecx.workspace.domain.models.Workspace;

import gen.org.tkit.onecx.workspace.rs.user.model.UserWorkspaceMenuItemDTO;
import gen.org.tkit.onecx.workspace.rs.user.model.UserWorkspaceMenuStructureDTO;

@Mapper
public interface UserMenuMapper {

    @Mapping(target = "removeI18nItem", ignore = true)
    @Mapping(target = "removeChildrenItem", ignore = true)
    UserWorkspaceMenuItemDTO mapTreeItem(MenuItem entity);

    default UserWorkspaceMenuStructureDTO empty(String workspaceName) {
        return new UserWorkspaceMenuStructureDTO().workspaceName(workspaceName).menu(List.of());
    }

    default UserWorkspaceMenuStructureDTO mapTree(Workspace workspace, Collection<MenuItem> entities,
            Map<String, Set<String>> mapping, Set<String> roles, Set<String> mappingKeys) {
        UserWorkspaceMenuStructureDTO dto = empty(workspace.getName());

        if (entities.isEmpty()) {
            return dto;
        }
        Set<MenuItem> items;
        if (!mappingKeys.isEmpty()) {
            items = entities.stream()
                    .filter(m -> m.getParentId() == null)
                    .filter(m -> mappingKeys.contains(m.getKey()))
                    .collect(Collectors.toSet());
        } else {
            items = entities.stream().filter(m -> m.getParentId() == null)
                    .collect(Collectors.toSet());
        }
        items = filterMenu(items, mapping, roles, workspace.getBaseUrl());
        if (items.isEmpty()) {
            return dto;
        }

        return dto.menu(items.stream().map(this::mapTreeItem).toList());
    }

    default String updateInternalUrl(String workspaceUrl, String menuItemUrl, Boolean isExternal, StringSubstitutor sub) {
        if (Boolean.TRUE.equals(isExternal)) {
            return sub.replace(menuItemUrl);
        } else {
            return sub.replace(workspaceUrl + menuItemUrl);
        }
    }

    default Set<MenuItem> filterMenu(Set<MenuItem> items, Map<String, Set<String>> mapping, Set<String> roles,
            String workspaceUrl) {
        Set<MenuItem> tmp = new HashSet<>(items);
        final var sub = new StringSubstitutor(System.getenv());
        tmp.forEach(rootItem -> {
            var mr = mapping.get(rootItem.getId());
            if (mr == null || mr.stream().noneMatch(roles::contains)) {
                items.remove(rootItem);
            } else {
                if (rootItem.getChildren() != null && !rootItem.getChildren().isEmpty()) {
                    filterChildren(rootItem, mapping, roles, workspaceUrl, sub);
                } else {
                    rootItem.setUrl(updateInternalUrl(workspaceUrl, rootItem.getUrl(), rootItem.isExternal(), sub));
                }
            }
        });

        return items;
    }

    default void filterChildren(MenuItem menuItem, Map<String, Set<String>> mapping, Set<String> roles, String workspaceUrl,
            StringSubstitutor sub) {
        Set<MenuItem> items = new HashSet<>(menuItem.getChildren());
        items.forEach(child -> {
            var mr = mapping.get(child.getId());
            if (mr == null || mr.stream().noneMatch(roles::contains)) {
                menuItem.getChildren().remove(child);
            } else {
                if (child.getChildren() != null && !child.getChildren().isEmpty()) {
                    filterChildren(child, mapping, roles, workspaceUrl, sub);
                } else {
                    child.setUrl(updateInternalUrl(workspaceUrl, child.getUrl(), child.isExternal(), sub));
                }
            }
        });
    }
}
