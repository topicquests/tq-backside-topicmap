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
package org.topicquests.backside.servlet.apps.tm;

import java.util.*;

import org.topicquests.backside.servlet.ServletEnvironment;
import org.topicquests.backside.servlet.apps.tm.api.ISocialBookmarkLegend;
import org.topicquests.backside.servlet.apps.tm.api.ITagModel;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;
import org.topicquests.ks.api.ICoreIcons;
import org.topicquests.ks.tm.api.IDataProvider;
import org.topicquests.ks.api.ITicket;
import org.topicquests.ks.api.INodeTypes;
import org.topicquests.ks.tm.api.IProxy;
import org.topicquests.ks.tm.api.IProxyModel;

/**
 * @author park
 *
 */
public class TagModel implements ITagModel {
	private ServletEnvironment environment;
	private IDataProvider topicMap;
	private IProxyModel nodeModel;

	/**
	 * 
	 */
	public TagModel(ServletEnvironment env) {
		environment = env;
		topicMap = environment.getTopicMapEnvironment().getDataProvider();
		nodeModel = environment.getTopicMapEnvironment().getProxyModel();
	}

	/* (non-Javadoc)
	 * @see org.topicquests.backside.servlet.apps.tm.api.ITagModel#addTagsToNode(org.topicquests.model.api.node.IProxy, java.lang.String[], org.topicquests.model.api.ITicket)
	 */
	@Override
	public IResult addTagsToNode(IProxy node, List<String> tagNames,
			ITicket credentials) {
		String userId = credentials.getUserLocator();
		IResult result = new ResultPojo();
		IResult r;
		int len = tagNames.size();
		String name,lox;
		String provenanceLocator = null; //TODO
		String subjectRoleLocator = null; //TODO
		String objectRoleLocator = null; //
		IProxy tag;
		for (int i=0;i<len;i++) {
			name = tagNames.get(i);
			lox = tagNameToLocator(name);
			//do we already know this tag?
			r = topicMap.getNode(lox, credentials);
			if (r.hasError())
				result.addErrorString(r.getErrorString());
			tag = (IProxy)r.getResultObject();
			if (tag == null) {
				//create a tag
				environment.logDebug("TagModel.addTagToNode-1 "+lox+" "+userId);
				System.out.println("TagModel.addTagToNode-1 "+lox+" "+userId);
				tag = nodeModel.newInstanceNode(lox, INodeTypes.TAG_TYPE, name, "", "en", userId, 
						ICoreIcons.TAG_SM, ICoreIcons.TAG, false);
				r = topicMap.putNode(tag);
				if (r.hasError())
					result.addErrorString(r.getErrorString());
			}
			//relate the tag to the user
			r = topicMap.getNode(userId, credentials);
			if (r.hasError())
				result.addErrorString(r.getErrorString());
			IProxy user = (IProxy)r.getResultObject();
			if (user != null) {
				environment.logDebug("TagModel.addTagToNode-2 "+tag+" "+user);
				System.out.println("TagModel.addTagToNode-2 "+tag+" "+user);
				r = nodeModel.relateExistingNodes(tag, user, ISocialBookmarkLegend.TAG_USER_RELATION_TYPE,
						subjectRoleLocator, objectRoleLocator,
						userId, provenanceLocator,
						ICoreIcons.RELATION_ICON_SM, ICoreIcons.RELATION_ICON, false, false);
				if (r.hasError())
					result.addErrorString(r.getErrorString());
			}
			//relate the tag to the topic
			environment.logDebug("TagModel.addTagToNode-3 "+tag+" "+node);
			System.out.println("TagModel.addTagToNode-3 "+tag+" "+node);
			r = nodeModel.relateExistingNodes(tag, node, ISocialBookmarkLegend.TAG_BOOKMARK_RELATION_TYPE, 
					subjectRoleLocator, objectRoleLocator, userId, provenanceLocator,
					ICoreIcons.RELATION_ICON_SM, ICoreIcons.RELATION_ICON, false, false);
			if (r.hasError())
				result.addErrorString(r.getErrorString());
			//relating does all the puts
		}
		return result;
	}

	
	///////////////////////////
	// utililties
	///////////////////////////
	
	private String tagNameToLocator(String name) {
		String result = name;
		result = result.toLowerCase();
		result = result.replaceAll(" ", "_");
		result = result.replaceAll("'", "_");
		result = result.replaceAll(":", "_");
//		result = result.replaceAll("\+", "P");
//		result = result.replaceAll("-", "M");
		result = result+"_TAG";
		return result;
	}
}
