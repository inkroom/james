/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.mailbox.store.mail;

import java.util.Collection;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.james.mailbox.model.FetchGroup;
import org.apache.james.mailbox.model.FetchGroup.Profile;

import com.google.common.collect.ImmutableList;

public class FetchGroupConverter {
    /**
     * Use the passed {@link FetchGroup} and calculate the right
     * {@link MessageMapper.FetchType} for it
     */
    public static MessageMapper.FetchType getFetchType(FetchGroup group) {
        if (!group.getPartContentDescriptors().isEmpty()) {
            return MessageMapper.FetchType.FULL;
        }

        Collection<MessageMapper.FetchType> fetchTypes = group.profiles()
            .stream()
            .map(FetchGroupConverter::toFetchType)
            .collect(ImmutableList.toImmutableList());

        return reduce(fetchTypes);
    }

    public static MessageMapper.FetchType reduce(Collection<MessageMapper.FetchType> fetchTypes) {
        boolean full = fetchTypes.contains(MessageMapper.FetchType.FULL);
        boolean headers = fetchTypes.contains(MessageMapper.FetchType.HEADERS);
        boolean body = fetchTypes.contains(MessageMapper.FetchType.BODY);

        if (full) {
            return MessageMapper.FetchType.FULL;
        }
        if (headers && body) {
            return MessageMapper.FetchType.FULL;
        }
        if (headers) {
            return MessageMapper.FetchType.HEADERS;
        }
        if (body) {
            return MessageMapper.FetchType.BODY;
        }
        return MessageMapper.FetchType.METADATA;
    }

    private static MessageMapper.FetchType toFetchType(Profile profile) {
        switch (profile) {
            case HEADERS:
                return MessageMapper.FetchType.HEADERS;
            case BODY_CONTENT:
                return MessageMapper.FetchType.BODY;
            case FULL_CONTENT:
            case MIME_CONTENT:
            case MIME_HEADERS:
            case MIME_DESCRIPTOR:
                // If we need the mimedescriptor we MAY need the full profile later too.
                // This gives us no other choice then request it
                return MessageMapper.FetchType.FULL;
            default:
                throw new NotImplementedException("Unsupported FetchGroup Profile" + profile);
        }
    }
}
