/*
 *   Copyright [2020] [Harry0198]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package me.harry0198.mailme.conversations;

import me.harry0198.mailme.mail.Mail;
import org.bukkit.conversations.ConversationAbandonedEvent;

public final class ConversationAbandonedListener implements org.bukkit.conversations.ConversationAbandonedListener {

    @Override
    public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {
        Mail mail = (Mail) abandonedEvent.getContext().getSessionData("mail");
        if (mail != null)
            mail.sendMail();
    }
}
