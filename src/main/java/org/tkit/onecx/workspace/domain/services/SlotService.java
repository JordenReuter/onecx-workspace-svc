package org.tkit.onecx.workspace.domain.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.onecx.workspace.domain.daos.SlotDAO;
import org.tkit.onecx.workspace.domain.models.Slot;
import org.tkit.onecx.workspace.rs.internal.mappers.SlotMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.rs.internal.model.UpdateSlotRequestDTO;

@LogService
@ApplicationScoped
public class SlotService {

    @Inject
    SlotDAO dao;

    @Inject
    SlotMapper mapper;

    @Transactional
    public Slot saveUpdateSlot(Slot slot, UpdateSlotRequestDTO updateSlotRequestDTO) {
        var slotWithoutComponents = mapper.removeComponents(slot);
        dao.update(slotWithoutComponents);
        mapper.update(updateSlotRequestDTO, slot);
        return dao.update(slot);
    }
}
