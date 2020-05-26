package com.ns8.hybris.notifications.daos.impl;

import com.ns8.hybris.notifications.enums.Ns8MessageStatus;
import com.ns8.hybris.notifications.model.Ns8QueueMessageModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNs8QueueMessageDaoTest {

    private static final String MESSAGE_ID = "messageId";

    @InjectMocks
    private DefaultNs8QueueMessageDao testObj;

    @Mock
    private FlexibleSearchService flexibleSearchServiceMock;
    @Captor
    private ArgumentCaptor<FlexibleSearchQuery> queryArgumentCaptor;
    @Mock
    private SearchResult searchResultMock;
    @Mock
    private Ns8QueueMessageModel message1Mock, message2Mock;

    @Before
    public void setUp() {
        when(flexibleSearchServiceMock.search(queryArgumentCaptor.capture())).thenReturn(searchResultMock);
    }

    @Test
    public void findPendingNs8QueueMessages_WhenThereAreMessages_ShouldReturnThem() {

        when(searchResultMock.getResult()).thenReturn(Arrays.asList(message1Mock, message2Mock));

        final List<Ns8QueueMessageModel> result = testObj.findPendingNs8QueueMessages();

        verify(flexibleSearchServiceMock).search(queryArgumentCaptor.capture());

        final FlexibleSearchQuery captorValue = queryArgumentCaptor.getValue();
        assertThat(captorValue.getQueryParameters()).containsOnlyKeys("status");
        assertThat(captorValue.getQueryParameters()).containsValues(Ns8MessageStatus.PENDING);

        assertThat(result).containsExactly(message1Mock, message2Mock);
    }

    @Test
    public void findNs8QueueMessageById_WhenTheMessageWithSameIdFound_ShouldReturnIt() {
        when(searchResultMock.getCount()).thenReturn(1);
        when(searchResultMock.getResult()).thenReturn(singletonList(message1Mock));

        final Optional<Ns8QueueMessageModel> result = testObj.findNs8QueueMessageById(MESSAGE_ID);

        verify(flexibleSearchServiceMock).search(queryArgumentCaptor.capture());
        final FlexibleSearchQuery captorValue = queryArgumentCaptor.getValue();
        assertThat(captorValue.getQueryParameters()).containsOnlyKeys("messageId");
        assertThat(captorValue.getQueryParameters()).containsValues(MESSAGE_ID);

        assertThat(result.get()).isEqualTo(message1Mock);
    }

    @Test
    public void findNs8QueueMessageById_WhenTheMessageWithSameIdNotFound_ShouldReturnEmpty() {
        when(searchResultMock.getCount()).thenReturn(0);
        when(searchResultMock.getResult()).thenReturn(singletonList(message1Mock));

        final Optional<Ns8QueueMessageModel> result = testObj.findNs8QueueMessageById(MESSAGE_ID);

        verify(flexibleSearchServiceMock).search(queryArgumentCaptor.capture());
        final FlexibleSearchQuery captorValue = queryArgumentCaptor.getValue();
        assertThat(captorValue.getQueryParameters()).containsOnlyKeys("messageId");
        assertThat(captorValue.getQueryParameters()).containsValues(MESSAGE_ID);

        assertThat(result.isEmpty()).isTrue();
    }

}
