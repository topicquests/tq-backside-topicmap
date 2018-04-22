/*
 * Copyright 2015, TopicQuests
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.topicquests.backside.servlet.apps.tm.api;

import java.util.List;

import org.topicquests.support.api.IResult;
import org.topicquests.ks.api.ITicket;
import org.topicquests.ks.tm.api.IProxy;

/**
 * @author park
 *
 */
public interface ITagModel {

	/**
	 * Convert <code>tagNames</code> into tag locators then
	 * create or reuse tags and add them to the node.
	 * @param node
	 * @param tagNames
	 * @param credentials
	 * @return
	 */
	IResult addTagsToNode(IProxy node, List<String> tagNames, ITicket credentials);
}
