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

import java.util.UUID;

import org.topicquests.backside.servlet.ServletEnvironment;
import org.topicquests.backside.servlet.api.IErrorMessages;
import org.topicquests.backside.servlet.apps.BaseModel;
import org.topicquests.backside.servlet.apps.tm.api.ISocialBookmarkLegend;
import org.topicquests.backside.servlet.apps.tm.api.IStructuredConversationModel;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;
import org.topicquests.ks.TicketPojo;
import org.topicquests.ks.api.ICoreIcons;
import org.topicquests.ks.api.ITQCoreOntology;
import org.topicquests.ks.tm.api.IDataProvider;
import org.topicquests.ks.api.ITicket;
import org.topicquests.ks.api.INodeTypes;
import org.topicquests.ks.tm.api.IParentChildContainer;
import org.topicquests.ks.tm.api.IProxy;
import org.topicquests.ks.tm.api.IProxyModel;

/**
 * @author jackpark
 *
 */
public class StructuredConversationModel extends BaseModel implements IStructuredConversationModel {
	private ITicket credentials;

	/**
	 * 
	 */
	public StructuredConversationModel(ServletEnvironment env) {
		super(env);
		credentials = new TicketPojo(ITQCoreOntology.SYSTEM_USER);
	}


	/* (non-Javadoc)
	 * @see org.topicquests.backside.servlet.apps.tm.api.IStructuredConversationModel#newConversationNode(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public IResult newConversationNode(String nodeType, String parentLocator, String contextLocator, String locator, String label, String details,
			String language, String url, String userId, boolean isPrivate) {
		String lox = locator;
		if (lox == null) 
			lox = UUID.randomUUID().toString();
		String smallIcon = null;
		String largeIcon = null;
		if (nodeType.equals(INodeTypes.CONVERSATION_MAP_TYPE)) {
			smallIcon = ICoreIcons.MAP_SM;
			largeIcon = ICoreIcons.MAP;
		} else if (nodeType.equals(INodeTypes.ISSUE_TYPE)) {
			smallIcon = ICoreIcons.ISSUE_SM;
			largeIcon = ICoreIcons.ISSUE;
		} else if (nodeType.equals(INodeTypes.POSITION_TYPE)) {
			smallIcon = ICoreIcons.POSITION_SM;
			largeIcon = ICoreIcons.POSITION;
		} else if (nodeType.equals(INodeTypes.PRO_TYPE)) {
			smallIcon = ICoreIcons.PRO_SM;
			largeIcon = ICoreIcons.PRO;
		} else if (nodeType.equals(INodeTypes.CON_TYPE)) {
			smallIcon = ICoreIcons.CON_SM;
			largeIcon = ICoreIcons.CON;
		} else if (nodeType.equals(INodeTypes.RESOURCE_TYPE)) {//IBIS Reference Node
			smallIcon = ICoreIcons.LINK_SM;
			largeIcon = ICoreIcons.LINK;
		} else if (nodeType.equals(INodeTypes.CHALLENGE_TYPE)) {
			smallIcon = ICoreIcons.CHALLENGE_SM;
			largeIcon = ICoreIcons.CHALLENGE_SM;
		}
		else {
			// this is an error condition -- bad NodeType
			environment.logError("StructuredConversationModel "+IErrorMessages.BAD_NODE_TYPE+" "+userId+" | "+label, null);
			IResult result = new ResultPojo();
			result.addErrorString(IErrorMessages.BAD_NODE_TYPE);
			return result;
		}
		return createNode(nodeType, lox, parentLocator, contextLocator, label, details, language,
							smallIcon, largeIcon, url, userId, isPrivate);
	}

	private IResult createNode(String nodeType, String locator, String parentLocator, String contextLocator,
					String label, String details, String language, String smallIcon, String largeIcon, String url,
					String userLocator, boolean isPrivate) {
		IResult result = new ResultPojo();
		IResult r = null;
		String provenanceLocator = null;//ToDO
		IProxy n = nodeModel.newInstanceNode(locator, nodeType, label, details, language, userLocator,
				provenanceLocator,
				smallIcon, largeIcon, isPrivate);
		System.out.println("SCM-1 "+parentLocator+" "+contextLocator);
		//TODO
		// We have an obligation to see if a parentLocator was passed in without a
		// contextLocator -- which would be an error condition
		if (parentLocator != null &&
			!parentLocator.equals("") &&
			contextLocator != null &&
			!contextLocator.equals("")) {
			
			r = topicMap.getNode(parentLocator, credentials);
			if (r.hasError()) {
				result.addErrorString(r.getErrorString());
			}
			System.out.println("SCM-2 "+r.getErrorString()+" | "+r.getResultObject());
			IProxy parent = (IProxy)r.getResultObject();
			if (parent != null) {
				// nodeModel is not implemented yet
				//nodeModel.addParentNode(n, contextLocator, parent.getSmallImage(), parentLocator,  parent.getLabel(language));
				//String contextLocator, String smallIcon, String locator, String subject
				((IParentChildContainer)n).addParentNode(contextLocator, parentLocator);
				((IParentChildContainer)parent).addChildNode(contextLocator, n.getLocator(), null);
				r = topicMap.putNode(parent);
				if (r.hasError()) {
					result.addErrorString(r.getErrorString());
				}
			} else {
				//TODO this is a really bad situation -- missing parent
				environment.logError("StructuredConversationModel Missing Parent "+parentLocator, null);
			}
		}
		if (url != null && !url.equals(""))
			n.setURL(url);
		r = topicMap.putNode(n);
		if (r.hasError()) {
			result.addErrorString(r.getErrorString());
		}
		r = relateNodeToUser(n, userLocator, provenanceLocator, credentials);
		if (r.hasError())
			result.addErrorString(r.getErrorString());
		result.setResultObject(n);
		return result;
	}


	@Override
	public IResult transcludeChildNode(String parentLocator, String contextLocator, String childLocator,
			String language, ITicket credentials) {
		IResult result = new ResultPojo();
		IResult r = topicMap.getNode(parentLocator, credentials);
		if (r.hasError()) result.addErrorString(r.getErrorString());
		IProxy parent = (IProxy)r.getResultObject();
		r = topicMap.getNode(childLocator, credentials);
		if (r.hasError()) result.addErrorString(r.getErrorString());
		IProxy child = (IProxy)r.getResultObject();
		((IParentChildContainer)child).addParentNode(contextLocator, parentLocator);
		((IParentChildContainer)parent).addChildNode(contextLocator, childLocator, childLocator); //TODO ???
		child.doUpdate();
		parent.doUpdate();
		//r = topicMap.updateNode(child, true);
		//if (r.hasError()) result.addErrorString(r.getErrorString());
		//r = topicMap.updateNode(parent, true);
		//if (r.hasError()) result.addErrorString(r.getErrorString());
		environment.logDebug("StructuredConversationModel.transcludeChildNodee "+result.getErrorString());
		return result;
	}
}
