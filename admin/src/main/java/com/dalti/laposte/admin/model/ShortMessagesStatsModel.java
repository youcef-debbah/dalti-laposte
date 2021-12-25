package com.dalti.laposte.admin.model;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.dalti.laposte.core.repositories.ShortMessage;
import com.dalti.laposte.core.repositories.SmsRepository;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dz.jsoftware95.silverbox.android.middleware.RepositoryModel;

@HiltViewModel
public class ShortMessagesStatsModel extends RepositoryModel<SmsRepository> {

    private final LiveData<Integer> messagesCount;
    private final LiveData<Integer> ooredooMessagesCount;
    private final LiveData<Integer> mobilisMessagesCount;
    private final LiveData<Integer> djezzyMessagesCount;

    private final Statistics sentMessages;

    @Inject
    public ShortMessagesStatsModel(@NotNull Application application,
                                   @NotNull SmsRepository repository) {
        super(application, repository);
        messagesCount = repository.countMessages();
        ooredooMessagesCount = repository.countOoredooMessages();
        mobilisMessagesCount = repository.countMobilisMessages();
        djezzyMessagesCount = repository.countDjezzyMessages();
        sentMessages = new Statistics(repository, ShortMessage.OK_STATE);
    }

    public LiveData<Integer> getMessagesCount() {
        return messagesCount;
    }

    public LiveData<Integer> getOoredooMessagesCount() {
        return ooredooMessagesCount;
    }

    public LiveData<Integer> getMobilisMessagesCount() {
        return mobilisMessagesCount;
    }

    public LiveData<Integer> getDjezzyMessagesCount() {
        return djezzyMessagesCount;
    }

    public Statistics getSentMessages() {
        return sentMessages;
    }

    public static final class Statistics {
        private final LiveData<Integer> messagesCount;
        private final LiveData<Integer> ooredooMessagesCount;
        private final LiveData<Integer> mobilisMessagesCount;
        private final LiveData<Integer> djezzyMessagesCount;
        private final String outcome;

        public Statistics(@NotNull SmsRepository repository, int code) {
            outcome = SmsRepository.getOutcome(code);
            messagesCount = repository.countMessages(code);
            ooredooMessagesCount = repository.countOoredooMessages(code);
            mobilisMessagesCount = repository.countMobilisMessages(code);
            djezzyMessagesCount = repository.countDjezzyMessages(code);
        }

        public LiveData<Integer> getMessagesCount() {
            return messagesCount;
        }

        public LiveData<Integer> getOoredooMessagesCount() {
            return ooredooMessagesCount;
        }

        public LiveData<Integer> getMobilisMessagesCount() {
            return mobilisMessagesCount;
        }

        public LiveData<Integer> getDjezzyMessagesCount() {
            return djezzyMessagesCount;
        }

        public String getOutcome() {
            return outcome;
        }

        @Override
        public String toString() {
            return outcome + "_STATISTICS{" +
                    "messages=" + messagesCount +
                    ", ooredooMessages=" + ooredooMessagesCount +
                    ", mobilisMessages=" + mobilisMessagesCount +
                    ", djezzyMessages=" + djezzyMessagesCount +
                    '}';
        }
    }
}
