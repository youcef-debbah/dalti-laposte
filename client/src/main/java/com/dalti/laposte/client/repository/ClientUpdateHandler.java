package com.dalti.laposte.client.repository;

import androidx.annotation.AnyThread;

import com.dalti.laposte.core.repositories.AbstractUpdateHandler;
import com.dalti.laposte.core.repositories.DashboardRepository;
import com.dalti.laposte.core.repositories.ProgressRepository;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.util.QueueUtils;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import dz.jsoftware95.queue.common.GlobalConf;
import dz.jsoftware95.queue.common.PostOfficeAvailability;
import dz.jsoftware95.silverbox.android.common.StringUtil;

@Singleton
@AnyThread
public class ClientUpdateHandler extends AbstractUpdateHandler {

    Lazy<DashboardRepository> dashboardRepository;
    Lazy<ProgressRepository> progressRepository;

    @Inject
    @AnyThread
    public ClientUpdateHandler(Lazy<DashboardRepository> dashboardRepository,
                               Lazy<ProgressRepository> progressRepository) {
        this.dashboardRepository = dashboardRepository;
        this.progressRepository = progressRepository;
    }

    @Override
    protected void onData(Map<String, String> data) {
        super.onData(data);
        String label = StringUtil.getString(data, GlobalConf.MESSAGE_LABEL);
        if (GlobalConf.NO_CACHE_LABEL_VALUE.equals(label)) {
            Teller.info("client invalidation request received");
            QueueUtils.requestCacheInvalidation();
        } else if (GlobalConf.PING_LABEL_VALUE.equals(label)) {
            Teller.info("ping received");
            QueueUtils.requestPong();
        } else {
            Long serviceID = StringUtil.parseLong(data.get(GlobalConf.SERVICE_KEY));
            Teller.info("client update received for service#" + serviceID + " " + PostOfficeAvailability.from(StringUtil.parseInteger(data.get(GlobalConf.AVAILABILITY_KEY))));
            dashboardRepository.get().handleServiceUpdate(serviceID, data);
            if (serviceID != null)
                progressRepository.get().handleClientNotifications(serviceID, data);
        }
    }
}
