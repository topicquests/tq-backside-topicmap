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

import net.minidev.json.JSONObject;

import org.topicquests.support.api.IResult;
import org.topicquests.ks.api.ITicket;

/**
 * @author park
 *
 */
public interface ITopicMapModel {
	////////////////
	// Additinoal Keys
	////////////////
	public static final String
		MY_CONVERSATIONS_LIST		= "myCons";
	////////////////
	// General TopicMap handlers
	//NOTE about <em>version</em>
	//  If checking version, the system will issue
	//  an Exception with the message <em>OptimisticLockException</ema>
	//  if the version number in the saved node is less than that which exists
	//  in the index.
	////////////////
	
	/**
	 * Store this <code>topic</code> to the topicmap
	 * @param topic
	 * @return
	 */
	IResult putTopic(JSONObject topic);
	
	/**
	 * <code>topic</code> must be a full ISubjectProxy object
	 * @param topic
	 * @param checkVersion
	 * @return
	 */
//	IResult updateTopic(JSONObject topic, boolean checkVersion);
	
	/**
	 * Simple way to update a topic's text fields
	 * @param locator
	 * @param label
	 * @param description
	 * @param language
	 * @param checkVersion
	 * @param credentials
	 * @return
	 */
	IResult updateTopicTextFields(String locator, String label,
			String description, String language, boolean checkVersion, ITicket credentials);
	
	IResult getTopic(String topicLocator, ITicket credentials);
	
	IResult removeTopic(String topicLocator, ITicket credentials);
	
	/**
	 * <p>Run a text query</p>
	 * <p>NOTE query must include start, count, sortBy and sortDir</p>
	 * <p>NOTE: a query is not a JSON string; we are using <code>query</code>
	 * as a carrier: actually query string must look like:<br/>
	 * "query":"<query string>"
	 * 
	 * @param query
	 * @param credentials
	 * @return
	 */
	IResult query(JSONObject query, ITicket credentials);
	
	/**
	 * <p>Note: a case could be made for 'listTopicsByURL' except that
	 * URL in this case refers to a lone PropertyType; the assumption
	 * is that, as a TopicMap, there can be one and only one topic
	 * with that identity property.<p>
	 * <p>There are, in fact, cases where this will return a list of proxies</p>
	 *
	 * @param url
	 * @param credentials
	 * @return
	 * 
	 */
	IResult getTopicByURL(String url, ITicket credentials);
	
	////////////////
	// Specialized TopicMap handlers
	////////////////
	
	IResult listSubclassTopics(String superClassLocator, int start, int count, ITicket credentials);
	
	IResult listInstanceTopics(String typeLocator, int start, int count, ITicket credentials);
	
	IResult listTopicsByKeyValue(String propertyKey, String value, int start, int count, String sortBy, String sortDir, ITicket credentials);
	
	//IResult listAllBlogPosts(int start, int count, ITicket credentials);
	
	//IResult listBlogPostsByUser(String userId, int start, int count, ITicket credentials);
	
	IResult getBookmarkByURL(String url, ITicket credentials);
	
	/**
	 * <p>Full text search.</p>
	 * <p>Searches in both label and description fields</p>
	 * @param queryString
	 * @param language
	 * @param start
	 * @param count
	 * @param sortBy can be <code>null</code>
	 * @param sortDir can be <code>null</code>
	 * @param credentials
	 * @return
	 */
	IResult listByFullTextQuery(String queryString, String language, int start, int count, String sortBy, String sortDir, ITicket credentials);
	
	/**
	 * <p>Performs a <em>MultiGet</em> on a list of tree child nodes contained by the topic
	 * identified by <code>rootNodeLocator</code>.</p>
	 * <p>Returns a JSONObject as a map: locator:JSONnode</p>
	 * <p>Essentially, this simply fetches all the child nodes of the given node, and
	 * returns a list of them</p>
	 * <p>Note: this is <em>not</em> a recursive walk down the tree</p>
	 * @param rootNodeLocator
	 * @param contextLocator
	 * @param credentials
	 * @return
	 */
	IResult listTreeChildNodesJSON(String rootNodeLocator, String contextLocator, ITicket credentials);
	
	/**
	 * Recursively walk down a tree from <code>rootNodeLocator</code> along child lines associated
	 * with <code>contextLocator</code>, which may or may not be the same as <code>rootNodeLocator</code>
	 * @param rootNodeLocator
	 * @param contextLocator
	 * @param credentials
	 * @return returns a {@link IConversationTreeStruct}
	 */
	IResult collectParentChildTree(String rootNodeLocator, String contextLocator, ITicket credentials);
	
    /////////////////////////////////
    // DSL for creating topics
    // The idea is that JSONObject theTopicShell allows
    // for a full specification of the entire topic
    // Start  with the regular features
	//  ADD section called "features": [ list of {} which specify key/value pairs]
    /////////////////////////////////

	/**
	 * <p>Allow for a simple shell topic, crafted at web clients, to be filled out to a full topic and persisted
	 * and returned.</p>
	 * <p>Also looks for a <em>extras</em> property.</p>
	 * <p><code>extras</code> field is a JSON object which an add known
	 * key/value pairs such as url, and dealing with parent and child nodes</p>
	 * <p><em>WARNING</em> key/value pairs will overwrite any existing
	 * key/value pairs in the node. It is wise to choose key/value pairs
	 * which do not already exist in an otherwise empty node.</p>
	 * @param theTopicShell
	 * @param credentials
	 * @return returns the node's JSONObject
	 */
	IResult newInstanceNode(JSONObject theTopicShell, ITicket credentials);
	
	IResult newSubclassNode(JSONObject theTopicShell, ITicket credentials);
	
	/**
	 * Cargo must include<br/>
	 * <li>nodeLocator</li>
	 * <li>property type-value(s) pair</li>
	 * <li>userId</li>
	 * @param cargo
	 * @param credentials
	 * @return
	 * @deprecated  in favor of FEATURES in <code>newInstanceNode</code>
	 * and <code>newSubclassNode</code>
	 */
	IResult addFeaturesToNode(JSONObject cargo, ITicket credentials);
	
	/**
	 * This will internally default to addRelation
	 * @param topicLocator
	 * @param pivotLocator
	 * @param pivotRelationType
	 * @param provenanceLocator TODO
	 * @param smallImagePath
	 * @param largeImagePath
	 * @param isTransclude
	 * @param isPrivate
	 * @param credentials
	 * @return
	 */
	IResult addPivot(String topicLocator, String pivotLocator, String pivotRelationType,
					 String provenanceLocator, String smallImagePath, String largeImagePath, boolean isTransclude, boolean isPrivate, ITicket credentials);

	IResult addRelation(String sourceLocator, String targetLocator, String relationTypeLocator,
			 String provenanceLocator, String smallImagePath, String largeImagePath, boolean isTransclude, boolean isPrivate, ITicket credentials);

	/**
	 * <p>For a given <code>tabLabel</code><br/>
	 *  1- See if tag exists.<br/>
	 *  2- If not, create tag node and pivot to <code>userId</code><br/>
	 *  3- Pivot to <code>bookmarkLocator</p>
	 * @param bookmarkLocator
	 * @param tagLabels
	 * @param language 
	 * @param credentials
	 * @return
	 */
	IResult findOrProcessTags(String bookmarkLocator, List<String> tagLabels, String language, ITicket credentials);
	
	/**
	 * Find bookmark node for <code>url</code> or otherwise make it and pivot to <code>userId</code>
	 * @param url
	 * @param title 
	 * @param details TODO
	 * @param language 
	 * @param userId
	 * @param provenanceLocator TODO
	 * @param tagLabels can be <code>null</code>
	 * @param credentials
	 * @return
	 */
	IResult findOrCreateBookmark(String url, String title, String details, String language, String userId, String provenanceLocator, JSONObject tagLabels, ITicket credentials);
	
    /**
     * List users in the TopicMap
     * @param start
     * @param count
     * @param credentials
     * @return
     */
    IResult listUserTopics(int start, int count, ITicket credentials);

    /**
     * Returns a tree rooted in <code>rootLocator</code>
     * 
     * @param rootLocator
     * @param maxDepth
     * @param start
     * @param count
     * @param sortBy TODO
     * @param sortDir TODO
     * @param credentials
     * @return
     */
    IResult getNodeTree(String rootLocator, int maxDepth, int start, int count, String sortBy, String sortDir, ITicket credentials);
    
	void shutDown();
}
