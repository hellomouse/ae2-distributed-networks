package net.hellomouse.ae2dn.definitions;

import appeng.api.integrations.igtooltip.ClientRegistration;
import appeng.api.integrations.igtooltip.CommonRegistration;
import appeng.api.integrations.igtooltip.TooltipProvider;
import net.hellomouse.ae2dn.AE2DistributedNetworks;
import net.hellomouse.ae2dn.RouteDistributorBlock;
import net.hellomouse.ae2dn.RouteDistributorBlockEntity;
import net.hellomouse.ae2dn.RouteDistributorDataProvider;
import net.minecraft.resources.ResourceLocation;

public class DNTooltipProviders implements TooltipProvider {
    @Override
    public void registerCommon(CommonRegistration registration) {
        registration.addBlockEntityData(
            ResourceLocation.fromNamespaceAndPath(AE2DistributedNetworks.MOD_ID, "route_distributor"),
            RouteDistributorBlockEntity.class,
            new RouteDistributorDataProvider()
        );
    }

    @Override
    public void registerClient(ClientRegistration registration) {
        registration.addBlockEntityBody(
            RouteDistributorBlockEntity.class,
            RouteDistributorBlock.class,
            ResourceLocation.fromNamespaceAndPath(AE2DistributedNetworks.MOD_ID, "route_distributor"),
            new RouteDistributorDataProvider()
        );
    }
}
