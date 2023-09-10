/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.containerpage.shared.rpc;

import org.opencms.ade.containerpage.shared.CmsCntPageData;
import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.ade.containerpage.shared.CmsCreateElementData;
import org.opencms.ade.containerpage.shared.CmsGroupContainer;
import org.opencms.gwt.CmsRpcException;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * The RPC service interface used by the container-page editor.<p>
 * 
 * @since 8.0.0
 */
public interface I_CmsContainerpageService extends RemoteService {

    /**
     * Adds an element specified by it's id to the favorite list.<p>
     * 
     * @param clientId the element id
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    void addToFavoriteList(String clientId) throws CmsRpcException;

    /**
     * Adds an element specified by it's id to the recent list.<p>
     * 
     * @param clientId the element id
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    void addToRecentList(String clientId) throws CmsRpcException;

    /**
     * To create a new element of the given type this method will check if a model resource needs to be selected, otherwise creates the new element.
     * Returns a bean containing either the new element data or a list of model resources to select.<p>
     * 
     * @param pageStructureId the container page structure id 
     * @param clientId the client id of the new element (this will be the structure id of the configured new resource)
     * @param resourceType the resource tape of the new element
     * @param locale the content locale
     * 
     * @return the bean containing either the new element data or a list of model resources to select
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    CmsCreateElementData checkCreateNewElement(
        CmsUUID pageStructureId,
        String clientId,
        String resourceType,
        String locale) throws CmsRpcException;

    /**
     * Creates a new element of the given type and returns the new element data containing structure id and site path.<p>
     * 
     * @param pageStructureId the container page structure id 
     * @param clientId the client id of the new element (this will be the structure id of the configured new resource)
     * @param resourceType the resource tape of the new element
     * @param modelResourceStructureId the model resource structure id
     * @param locale the content locale
     * 
     * @return the new element data containing structure id and site path
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    CmsContainerElement createNewElement(
        CmsUUID pageStructureId,
        String clientId,
        String resourceType,
        CmsUUID modelResourceStructureId,
        String locale) throws CmsRpcException;

    /**
     * Returns container element data by client id.<p>
     * 
     * @param  pageStructureId the container page structure id
     * @param reqParams optional request parameters
     * @param clientIds the requested element id's
     * @param containers the containers of the current page
     * @param locale the content locale
     * 
     * @return the element data
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    Map<String, CmsContainerElementData> getElementsData(
        CmsUUID pageStructureId,
        String reqParams,
        Collection<String> clientIds,
        Collection<CmsContainer> containers,
        String locale) throws CmsRpcException;

    /**
     * Gets the element data for an id and a map of settings.<p>
     * 
     * @param pageStructureId the container page structure id 
     * @param reqParams optional request parameters 
     * @param clientId the requested element ids 
     * @param settings the settings for which the element data should be loaded 
     * @param containers the containers of the current page 
     * @param locale the content locale
     * 
     * @return the element data 
     * 
     * @throws CmsRpcException if something goes wrong processing the request 
     */
    CmsContainerElementData getElementWithSettings(
        CmsUUID pageStructureId,
        String reqParams,
        String clientId,
        Map<String, String> settings,
        Collection<CmsContainer> containers,
        String locale) throws CmsRpcException;

    /**
     * Returns the container element data of the favorite list.<p>
     * 
     * @param pageStructureId the container page structure id
     * @param containers the containers of the current page
     * @param locale the content locale
     * 
     * @return the favorite list element data
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    List<CmsContainerElementData> getFavoriteList(
        CmsUUID pageStructureId,
        Collection<CmsContainer> containers,
        String locale) throws CmsRpcException;

    /**
     * Returns new container element data for the given resource type name.<p>
     * 
     * @param  pageStructureId the container page structure id
     * @param reqParams optional request parameters
     * @param resourceType the requested element resource type name
     * @param containers the containers of the current page
     * @param locale the content locale
     * 
     * @return the element data
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    CmsContainerElementData getNewElementData(
        CmsUUID pageStructureId,
        String reqParams,
        String resourceType,
        Collection<CmsContainer> containers,
        String locale) throws CmsRpcException;

    /**
     * Returns the container element data of the recent list.<p>
     * 
     * @param pageStructureId the container page structure id
     * @param containers the containers of the current page
     * @param locale the content locale
     * 
     * @return the recent list element data
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    List<CmsContainerElementData> getRecentList(
        CmsUUID pageStructureId,
        Collection<CmsContainer> containers,
        String locale) throws CmsRpcException;

    /**
     * Returns the initialization data.<p>
     * 
     * @return the initialization data
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    CmsCntPageData prefetch() throws CmsRpcException;

    /**
     * Saves the container-page.<p>
     * 
     * @param pageStructureId the container page structure id
     * @param containers the container-page's containers
     * @param locale the content locale
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    void saveContainerpage(CmsUUID pageStructureId, List<CmsContainer> containers, String locale)
    throws CmsRpcException;

    /**
     * Saves the favorite list.<p>
     * 
     * @param clientIds favorite list element id's
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    void saveFavoriteList(List<String> clientIds) throws CmsRpcException;

    /**
     * Saves a group-container element.<p>
     * 
     * @param pageStructureId the container page structure id
     * @param reqParams optional request parameters
     * @param groupContainer the group-container to save
     * @param containers the containers of the current page
     * @param locale the content locale
     * 
     * @return the data of the saved group container 
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    Map<String, CmsContainerElementData> saveGroupContainer(
        CmsUUID pageStructureId,
        String reqParams,
        CmsGroupContainer groupContainer,
        Collection<CmsContainer> containers,
        String locale) throws CmsRpcException;

    /**
     * Saves the recent list.<p>
     * 
     * @param clientIds recent list element id's
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    void saveRecentList(List<String> clientIds) throws CmsRpcException;

    /**
     * Saves the container-page in a synchronized RPC call.<p>
     * 
     * @param pageStructureId the container page structure id
     * @param containers the container-page's containers
     * @param locale the content locale
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    void syncSaveContainerpage(CmsUUID pageStructureId, List<CmsContainer> containers, String locale)
    throws CmsRpcException;
}