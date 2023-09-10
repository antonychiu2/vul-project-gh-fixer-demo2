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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.publish;

import org.opencms.ade.publish.CmsPublishGroupHelper.GroupAge;
import org.opencms.ade.publish.shared.CmsProjectBean;
import org.opencms.ade.publish.shared.CmsPublishGroup;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.ade.publish.shared.CmsPublishResource;
import org.opencms.ade.publish.shared.CmsPublishResourceInfo;
import org.opencms.db.CmsPublishList;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.I_CmsResource;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationPublishValidator;
import org.opencms.relations.CmsRelationValidatorInfoEntry;
import org.opencms.report.CmsHtmlReport;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.Maps;

/**
 * ADE publishing features.<p>
 * 
 * @since 8.0.0
 */
public class CmsPublish {

    /**
     * Just for passing around resources and their related together but not mixed up.<p>
     */
    private class ResourcesAndRelated {

        /** The related resources. */
        private Set<CmsResource> m_relatedResources = new HashSet<CmsResource>();

        /** The resources. */
        private Set<CmsResource> m_resources = new HashSet<CmsResource>();

        /**
         * Constructor.<p>
         */
        public ResourcesAndRelated() {

            // empty
        }

        /**
         * Checks if the given resource is present in at least one of the sets.<p>
         * 
         * @param resource the resource to test
         * 
         * @return <code>true</code> if the given resource is present in at least one of the sets
         */
        public boolean contains(CmsResource resource) {

            return m_resources.contains(resource) || m_relatedResources.contains(resource);
        }

        /**
         * Returns the related resources.<p>
         *
         * @return the related resources
         */
        public Set<CmsResource> getRelatedResources() {

            return m_relatedResources;
        }

        /**
         * Returns the resources.<p>
         *
         * @return the resources
         */
        public Set<CmsResource> getResources() {

            return m_resources;
        }
    }

    /** The number of day groups. */
    protected static final int GROUP_DAYS_NUMBER = 3;

    /** The gap between session groups. */
    protected static final int GROUP_SESSIONS_GAP = 8 * 60 * 60 * 1000;

    /** The number of session groups. */
    protected static final int GROUP_SESSIONS_NUMBER = 2;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPublish.class);

    /** The current cms context. */
    private final CmsObject m_cms;

    /** The current user workplace locale. */
    private final Locale m_workplaceLocale;

    /** The options. */
    private final CmsPublishOptions m_options;

    /** The user's resource publish list. */
    private ResourcesAndRelated m_resourceList;

    /**
     * Constructor with default options.<p>
     * 
     * @param cms the current cms context
     */
    public CmsPublish(CmsObject cms) {

        this(cms, new CmsPublishOptions());
    }

    /**
     * Constructor with options.<p>
     * 
     * @param cms the current cms context
     * @param options the options to use
     */
    public CmsPublish(CmsObject cms, CmsPublishOptions options) {

        m_cms = cms;
        m_workplaceLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms);
        m_options = options;
    }

    /**
     * Checks for possible broken links when the given list of resources would be published.<p>
     * 
     * @param pubResources list of resources to be published
     * 
     * @return a list of resources that would produce broken links when published 
     */
    public List<CmsPublishResource> getBrokenResources(List<CmsResource> pubResources) {

        List<CmsPublishResource> resources = new ArrayList<CmsPublishResource>();

        CmsPublishList publishList;
        try {
            publishList = OpenCms.getPublishManager().getPublishListAll(
                m_cms,
                pubResources,
                m_options.isIncludeSiblings(),
                true);
        } catch (CmsException e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
            return resources;
        }

        CmsRelationPublishValidator validator = new CmsRelationPublishValidator(m_cms, publishList);
        for (String resourceName : validator.keySet()) {
            CmsRelationValidatorInfoEntry infoEntry = validator.getInfoEntry(resourceName);
            try {
                CmsResource resource = m_cms.readResource(
                    m_cms.getRequestContext().removeSiteRoot(resourceName),
                    CmsResourceFilter.ALL);
                if (resource.getState().isDeleted()) {
                    for (CmsRelation relation : infoEntry.getRelations()) {
                        try {
                            CmsResource theResource = relation.getSource(m_cms, CmsResourceFilter.ALL);
                            CmsPublishResourceInfo info = new CmsPublishResourceInfo(
                                Messages.get().getBundle(m_workplaceLocale).key(Messages.GUI_BROKEN_LINK_ONLINE_0),
                                CmsPublishResourceInfo.Type.BROKENLINK);
                            // HACK: GWT serialization does not like unmodifiable collections :(
                            // Collections.singletonList(resourceToBean(resource, info, false, null)));
                            ArrayList<CmsPublishResource> relatedList = new ArrayList<CmsPublishResource>();
                            relatedList.add(resourceToBean(resource, info, false, null));
                            CmsPublishResource pubRes = resourceToBean(theResource, null, false, relatedList);
                            resources.add(pubRes);
                        } catch (CmsException e) {
                            // should never happen
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }
                } else {
                    try {
                        List<CmsPublishResource> related = new ArrayList<CmsPublishResource>();
                        for (CmsRelation relation : infoEntry.getRelations()) {
                            try {
                                CmsResource theResource = relation.getTarget(m_cms, CmsResourceFilter.ALL);
                                CmsPublishResource pubRes = resourceToBean(theResource, null, false, null);
                                related.add(pubRes);
                            } catch (CmsException e) {
                                CmsPublishResource pubRes = relationToBean(relation);
                                related.add(pubRes);
                                LOG.warn(e.getLocalizedMessage(), e);
                            }
                        }
                        CmsPublishResourceInfo info = new CmsPublishResourceInfo(
                            Messages.get().getBundle(m_workplaceLocale).key(Messages.GUI_RESOURCE_MISSING_ONLINE_0),
                            CmsPublishResourceInfo.Type.MISSING);
                        CmsPublishResource pubRes = resourceToBean(resource, info, false, related);
                        resources.add(pubRes);
                    } catch (Exception e) {
                        // should never happen
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            } catch (CmsException e) {
                // should never happen
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

        return resources;
    }

    /**
     * Returns the current user's manageable projects.<p>
     * 
     * @return the current user's manageable projects
     */
    public List<CmsProjectBean> getManageableProjects() {

        List<CmsProjectBean> manProjs = new ArrayList<CmsProjectBean>();

        List<CmsProject> projects;
        try {
            projects = OpenCms.getOrgUnitManager().getAllManageableProjects(m_cms, "", true);
        } catch (CmsException e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
            return manProjs;
        }

        for (CmsProject project : projects) {
            CmsProjectBean manProj = new CmsProjectBean(project.getUuid(), getOuAwareName(project.getName()));
            manProjs.add(manProj);
        }

        return manProjs;
    }

    /**
     * Returns the options.<p>
     *
     * @return the options
     */
    public CmsPublishOptions getOptions() {

        return m_options;
    }

    /**
     * Returns the list of publish groups with resources that can be published.<p>
     * 
     * @return the list of publish groups with resources that can be published
     */
    public List<CmsPublishGroup> getPublishGroups() {

        // first look for already published resources
        Set<CmsResource> published = getAlreadyPublishedResources();

        // then for resources without permission
        Set<CmsResource> exclude = new HashSet<CmsResource>(published);

        ResourcesAndRelated permissions = getResourcesWithoutPermissions(exclude);

        // and finally for locked resources
        exclude.addAll(permissions.getResources());
        exclude.addAll(permissions.getRelatedResources());

        ResourcesAndRelated locked = getBlockingLockedResources(exclude);

        // collect all direct resources that can not be published
        exclude.clear();
        exclude.addAll(published);
        exclude.addAll(permissions.getResources());
        exclude.addAll(locked.getResources());

        // update the publish list
        ResourcesAndRelated pubResources = new ResourcesAndRelated();
        pubResources.getResources().addAll(getPublishResources().getResources());
        pubResources.getResources().removeAll(exclude);
        pubResources.getRelatedResources().addAll(getPublishResources().getRelatedResources());
        pubResources.getRelatedResources().removeAll(permissions.getRelatedResources());
        pubResources.getRelatedResources().removeAll(locked.getRelatedResources());

        if (getPublishResources().getResources().isEmpty()) {
            // nothing to do
            return new ArrayList<CmsPublishGroup>();
        }

        List<CmsResource> resourcesWithoutTempfiles = new ArrayList<CmsResource>();
        for (CmsResource res : getPublishResources().getResources()) {
            if (!CmsResource.isTemporaryFileName(res.getRootPath())) {
                resourcesWithoutTempfiles.add(res);
            }
        }

        // sort the list
        List<CmsResource> sortedResources = new ArrayList<CmsResource>(resourcesWithoutTempfiles);
        Collections.sort(sortedResources, I_CmsResource.COMPARE_DATE_LAST_MODIFIED);

        // the resources the user can really publish
        Set<CmsResource> allPubRes = new HashSet<CmsResource>(pubResources.getRelatedResources());
        allPubRes.addAll(pubResources.getResources());

        List<CmsResource> pubList = new ArrayList<CmsResource>();
        try {
            pubList = OpenCms.getPublishManager().getUsersPubList(m_cms);
        } catch (CmsException e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
        }

        CmsPublishGroupHelper groupHelper = new CmsPublishGroupHelper(m_workplaceLocale);

        Map<Long, Integer> daysMap = groupHelper.computeDaysForResources(sortedResources);
        Map<GroupAge, List<CmsResource>> resourcesByAge = groupHelper.partitionPublishResourcesByAge(
            sortedResources,
            daysMap);
        List<List<CmsResource>> youngGroups = groupHelper.partitionYoungResources(resourcesByAge.get(GroupAge.young));
        List<List<CmsResource>> mediumGroups = groupHelper.partitionMediumResources(
            resourcesByAge.get(GroupAge.medium),
            daysMap);
        List<CmsResource> oldGroup = resourcesByAge.get(GroupAge.old);

        List<CmsPublishGroup> resultGroups = new ArrayList<CmsPublishGroup>();
        for (List<CmsResource> groupRes : youngGroups) {
            List<CmsPublishResource> groupPubRes = new ArrayList<CmsPublishResource>();
            for (CmsResource res : groupRes) {
                CmsPublishResource pubRes = createPublishResource(
                    res,
                    pubList,
                    allPubRes,
                    published,
                    permissions,
                    locked);
                groupPubRes.add(pubRes);
            }
            String name = groupHelper.getPublishGroupName(groupRes, GroupAge.young);
            resultGroups.add(new CmsPublishGroup(name, groupPubRes));
        }

        for (List<CmsResource> groupRes : mediumGroups) {
            List<CmsPublishResource> groupPubRes = new ArrayList<CmsPublishResource>();
            for (CmsResource res : groupRes) {
                CmsPublishResource pubRes = createPublishResource(
                    res,
                    pubList,
                    allPubRes,
                    published,
                    permissions,
                    locked);
                groupPubRes.add(pubRes);
            }
            String name = groupHelper.getPublishGroupName(groupRes, GroupAge.medium);
            resultGroups.add(new CmsPublishGroup(name, groupPubRes));
        }

        if (!oldGroup.isEmpty()) {
            String oldName = groupHelper.getPublishGroupName(oldGroup, GroupAge.old);
            List<CmsPublishResource> oldRes = new ArrayList<CmsPublishResource>();
            for (CmsResource res : oldGroup) {
                CmsPublishResource pubRes = createPublishResource(
                    res,
                    pubList,
                    allPubRes,
                    published,
                    permissions,
                    locked);
                oldRes.add(pubRes);
            }
            resultGroups.add(new CmsPublishGroup(oldName, oldRes));
        }

        return resultGroups;
    }

    /**
     * Publishes the given list of resources.<p>
     * 
     * @param resources list of resources to publish
     * 
     * @throws CmsException if something goes wrong
     */
    public void publishResources(List<CmsResource> resources) throws CmsException {

        CmsObject cms = m_cms;
        I_CmsReport report = new CmsHtmlReport(
            cms.getRequestContext().getLocale(),
            cms.getRequestContext().getSiteRoot());
        CmsPublishList publishList = OpenCms.getPublishManager().getPublishListAll(
            m_cms,
            resources,
            m_options.isIncludeSiblings(),
            true);
        OpenCms.getPublishManager().publishProject(m_cms, report, publishList);
    }

    /**
     * Creates a publish resource bean from the target information of a relation object.<p>
     * 
     * @param relation the relation to use
     *  
     * @return the publish resource bean for the relation target 
     */
    public CmsPublishResource relationToBean(CmsRelation relation) {

        return new CmsPublishResource(
            relation.getTargetId(),
            relation.getTargetPath(),
            relation.getTargetPath(),
            CmsResourceTypePlain.getStaticTypeName(),
            CmsResourceState.STATE_UNCHANGED,
            false,
            null,
            null);
    }

    /**
     * Removes the given resources from the user's publish list.<p>
     * 
     * @param idsToRemove list of structure ids identifying the resources to be removed
     * 
     * @throws CmsException if something goes wrong
     */
    public void removeResourcesFromPublishList(Collection<CmsUUID> idsToRemove) throws CmsException {

        OpenCms.getPublishManager().removeResourceFromUsersPubList(m_cms, idsToRemove);
    }

    /**
     * Returns already published resources.<p>
     * 
     * @return already published resources
     */
    protected Set<CmsResource> getAlreadyPublishedResources() {

        Set<CmsResource> resources = new HashSet<CmsResource>();
        for (CmsResource resource : getPublishResources().getResources()) {
            // we are interested just in not-changed resources
            if (!resource.getState().isUnchanged()) {
                continue;
            }
            resources.add(resource);
        }
        return resources;
    }

    /**
     * Returns locked resources that do not belong to the current user.<p>
     * 
     * @param exclude the resources to exclude
     * 
     * @return the locked and related resources
     * 
     * @see org.opencms.workplace.commons.CmsLock#getBlockingLockedResources
     */
    protected ResourcesAndRelated getBlockingLockedResources(Set<CmsResource> exclude) {

        CmsUser user = m_cms.getRequestContext().getCurrentUser();
        CmsLockFilter blockingFilter = CmsLockFilter.FILTER_ALL;
        blockingFilter = blockingFilter.filterNotLockableByUser(user);

        ResourcesAndRelated result = new ResourcesAndRelated();
        Map<String, CmsResource> cache1 = Maps.newHashMap();
        for (CmsResource resource : getPublishResources().getResources()) {
            // skip already blocking resources
            if (exclude.contains(resource)) {
                continue;
            }
            try {
                result.getResources().addAll(m_cms.getLockedResourcesWithCache(resource, blockingFilter, cache1));
            } catch (Exception e) {
                // error reading the resource list, should usually never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        for (CmsResource resource : getPublishResources().getRelatedResources()) {
            // skip already blocking resources
            if (exclude.contains(resource)) {
                continue;
            }
            try {
                result.getRelatedResources().addAll(m_cms.getLockedResourcesWithCache(resource, blockingFilter, cache1));
            } catch (Exception e) {
                // error reading the resource list, should usually never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return result;
    }

    /**
     * Returns the simple name if the ou is the same as the current user's ou.<p>
     * 
     * @param name the fully qualified name to check
     * 
     * @return the simple name if the ou is the same as the current user's ou
     */
    protected String getOuAwareName(String name) {

        String ou = CmsOrganizationalUnit.getParentFqn(name);
        if (ou.equals(m_cms.getRequestContext().getCurrentUser().getOuFqn())) {
            return CmsOrganizationalUnit.getSimpleName(name);
        }
        return CmsOrganizationalUnit.SEPARATOR + name;
    }

    /**
     * Returns the resources stored in the user's publish list.<p>
     * 
     * @return the resources stored in the user's publish list
     */
    protected ResourcesAndRelated getPublishResources() {

        if (m_resourceList != null) {
            return m_resourceList;
        }
        m_resourceList = new ResourcesAndRelated();
        try {
            if ((m_options.getProjectId() == null) || m_options.getProjectId().isNullUUID()) {
                // get the users publish list
                m_resourceList.getResources().addAll(OpenCms.getPublishManager().getUsersPubList(m_cms));
            } else {
                CmsProject project = m_cms.getRequestContext().getCurrentProject();
                try {
                    project = m_cms.readProject(m_options.getProjectId());
                } catch (Exception e) {
                    // can happen if the cached project was deleted
                    // so ignore and use current project
                }
                // get the project publish list
                CmsProject originalProject = m_cms.getRequestContext().getCurrentProject();
                try {
                    m_cms.getRequestContext().setCurrentProject(project);
                    m_resourceList.getResources().addAll(
                        OpenCms.getPublishManager().getPublishList(m_cms).getAllResources());
                } finally {
                    m_cms.getRequestContext().setCurrentProject(originalProject);
                }
            }
        } catch (CmsException e) {
            // error reading the publish list, should usually never happen
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            return m_resourceList;
        }
        if (m_options.isIncludeSiblings()) {
            for (CmsResource resource : new HashSet<CmsResource>(m_resourceList.getResources())) {
                // we are interested just in changed resources
                if (resource.getState().isUnchanged()) {
                    continue;
                }
                try {
                    m_resourceList.getResources().addAll(
                        m_cms.readSiblings(m_cms.getSitePath(resource), CmsResourceFilter.ALL_MODIFIED));
                } catch (CmsException e) {
                    // error reading resource siblings, should usually never happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                    continue;
                }
            }
        }
        if (m_options.isIncludeRelated()) {
            for (CmsResource resource : m_resourceList.getResources()) {
                // we are interested just in changed resources
                if (resource.getState().isUnchanged()) {
                    continue;
                }
                try {
                    // get and iterate over all related resources
                    for (CmsRelation relation : m_cms.getRelationsForResource(
                        resource,
                        CmsRelationFilter.TARGETS.filterStrong())) {

                        CmsResource target = null;
                        try {
                            target = relation.getTarget(m_cms, CmsResourceFilter.ALL);
                        } catch (CmsException e) {
                            // error reading a resource, should usually never happen
                            if (LOG.isErrorEnabled()) {
                                LOG.error(e.getLocalizedMessage(), e);
                            }
                            continue;
                        }
                        // we are interested just in changed resources
                        if (target.getState().isUnchanged()) {
                            continue;
                        }
                        // if already selected
                        if (m_resourceList.contains(target)) {
                            continue;
                        }
                        m_resourceList.getRelatedResources().add(target);
                    }
                } catch (CmsException e) {
                    // error reading a resource relations, should usually never happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                    continue;
                }
            }
        }
        return m_resourceList;
    }

    /**
     * Returns a string with a list of related resources in the publish list.<p>
     * 
     * @param resource the resource to use
     * @param resources the resources the user can really publish
     * @param published the already published resources
     * @param permissions the resource the current user does not have publish permissions for
     * @param locked the locked resources
     * 
     * @return a string with a list of related resources in the publish list, or <code>null</code> if none
     */
    protected List<CmsPublishResource> getRelatedResources(
        CmsResource resource,
        Set<CmsResource> resources,
        Set<CmsResource> published,
        ResourcesAndRelated permissions,
        ResourcesAndRelated locked) {

        List<CmsPublishResource> relatedResources = new ArrayList<CmsPublishResource>();
        try {
            // get and iterate over all related resources
            for (CmsRelation relation : m_cms.getRelationsForResource(
                resource,
                CmsRelationFilter.TARGETS.filterStrong())) {

                CmsResource target = null;
                try {
                    target = relation.getTarget(m_cms, CmsResourceFilter.ALL);
                } catch (CmsException e) {
                    // error reading a resource, should usually never happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                    continue;
                }
                // see if the source is a resource to be published
                CmsPublishResourceInfo info;
                if (resources.contains(target)) {
                    info = getResourceInfo(resource, published, permissions, locked);
                } else if (!target.getState().isUnchanged()) {
                    // a modified related resource can not be published
                    info = new CmsPublishResourceInfo(Messages.get().getBundle(m_workplaceLocale).key(
                        Messages.GUI_RELATED_RESOURCE_CAN_NOT_BE_PUBLISHED_0), CmsPublishResourceInfo.Type.RELATED);
                } else {
                    continue;
                }
                CmsPublishResource relatedResource = resourceToBean(target, info, false, null);
                relatedResources.add(relatedResource);
            }
        } catch (CmsException e) {
            // error reading a resource relations, should usually never happen
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return relatedResources;
    }

    /**
     * Returns the additional info for the given resource.<p>
     * 
     * @param resource the resource to use
     * @param published the already published resources
     * @param permissions the resource the current user does not have publish permissions for
     * @param locked the locked resources
     * 
     * @return the additional info for the given resource
     */
    protected CmsPublishResourceInfo getResourceInfo(
        CmsResource resource,
        Set<CmsResource> published,
        ResourcesAndRelated permissions,
        ResourcesAndRelated locked) {

        String info = null;
        CmsPublishResourceInfo.Type infoType = null;
        try {
            if (published.contains(resource)) {
                // TODO: get the real publish data
                String publishUser = getOuAwareName(m_cms.readUser(resource.getUserLastModified()).getName());
                Date publishDate = new Date(resource.getDateLastModified());
                info = Messages.get().getBundle(m_workplaceLocale).key(
                    Messages.GUI_RESOURCE_PUBLISHED_BY_2,
                    publishUser,
                    publishDate);
                infoType = CmsPublishResourceInfo.Type.PUBLISHED;
            } else if (permissions.contains(resource)) {
                info = Messages.get().getBundle(m_workplaceLocale).key(Messages.GUI_RESOURCE_NOT_ENOUGH_PERMISSIONS_0);
                infoType = CmsPublishResourceInfo.Type.PERMISSIONS;
            } else if (locked.contains(resource)) {
                CmsLock lock = m_cms.getLock(resource);
                info = Messages.get().getBundle(m_workplaceLocale).key(
                    Messages.GUI_RESOURCE_LOCKED_BY_2,
                    getOuAwareName(m_cms.readUser(lock.getUserId()).getName()),
                    getOuAwareName(lock.getProject().getName()));
                infoType = CmsPublishResourceInfo.Type.LOCKED;
            }
        } catch (Exception e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
        }
        return infoType == null ? null : new CmsPublishResourceInfo(info, infoType);
    }

    /**
     * Formats the given resource path depending on the site root.<p>
     * 
     * @param rootPath the resource path to format
     * @param siteRoot the site root
     * 
     * @return the formatted resource path
     */
    protected String getResourceName(String rootPath, String siteRoot) {

        if (rootPath.startsWith(siteRoot)) {
            // same site
            rootPath = rootPath.substring(siteRoot.length());
        } else {
            // other site
            String site = OpenCms.getSiteManager().getSiteRoot(rootPath);
            String siteName = site;
            if (site != null) {
                rootPath = rootPath.substring(site.length());
                siteName = OpenCms.getSiteManager().getSiteForSiteRoot(site).getTitle();
            } else {
                siteName = "/";
            }
            rootPath = org.opencms.workplace.commons.Messages.get().getBundle(m_workplaceLocale).key(
                org.opencms.workplace.commons.Messages.GUI_PUBLISH_SITE_RELATION_2,
                new Object[] {siteName, rootPath});
        }
        return rootPath;
    }

    /**
     * Returns the sublist of the publish list with resources without publish permissions.<p>
     * 
     * @param exclude the resources to exclude
     * 
     * @return the list with resources without publish permissions
     */
    protected ResourcesAndRelated getResourcesWithoutPermissions(Set<CmsResource> exclude) {

        Set<CmsUUID> projectIds = new HashSet<CmsUUID>();
        try {
            for (CmsProject project : OpenCms.getOrgUnitManager().getAllManageableProjects(m_cms, "", true)) {
                projectIds.add(project.getUuid());
            }
        } catch (CmsException e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
        }

        ResourcesAndRelated result = new ResourcesAndRelated();
        for (CmsResource resource : getPublishResources().getResources()) {
            // skip already blocking resources
            if (exclude.contains(resource)) {
                continue;
            }
            try {
                if (!projectIds.contains(resource.getProjectLastModified())
                    && !m_cms.hasPermissions(resource, CmsPermissionSet.ACCESS_DIRECT_PUBLISH)) {
                    result.getResources().add(resource);
                }
            } catch (Exception e) {
                // error reading the permissions, should usually never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        for (CmsResource resource : getPublishResources().getRelatedResources()) {
            // skip already blocking resources
            if (exclude.contains(resource)) {
                continue;
            }
            try {
                if (!m_cms.hasPermissions(resource, CmsPermissionSet.ACCESS_DIRECT_PUBLISH)) {
                    result.getRelatedResources().add(resource);
                }
            } catch (Exception e) {
                // error reading the resource list, should usually never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return result;
    }

    /**
     * Creates a publish resource bean instance from the given parameters.<p>
     * 
     * @param resource the resource
     * @param info the publish information, if any
     * @param removable if removable
     * @param related the list of related resources
     * 
     * @return the publish resource bean
     */
    protected CmsPublishResource resourceToBean(
        CmsResource resource,
        CmsPublishResourceInfo info,
        boolean removable,
        List<CmsPublishResource> related) {

        CmsResourceUtil resUtil = new CmsResourceUtil(m_cms, resource);

        CmsPublishResource pubResource = new CmsPublishResource(
            resource.getStructureId(),
            resUtil.getFullPath(),
            resUtil.getTitle(),
            resUtil.getResourceTypeName(),
            resource.getState(),
            removable,
            info,
            related);
        return pubResource;
    }

    /**
     * Creates a {@link CmsPublishResource} from a {@link CmsResource}.<p> 
     * 
     * @param resource the resource to convert
     * @param pubList a publish list
     * @param allPubRes a set of all publish resources
     * @param published a set of already published resources
     * @param permissions resources for which we don't have the permissions
     * @param locked resources which are locked by another user 
     * 
     * @return a publish resource bean 
     */
    private CmsPublishResource createPublishResource(
        CmsResource resource,
        List<CmsResource> pubList,
        Set<CmsResource> allPubRes,
        Set<CmsResource> published,
        ResourcesAndRelated permissions,
        ResourcesAndRelated locked) {

        List<CmsPublishResource> related = getRelatedResources(resource, allPubRes, published, permissions, locked);
        CmsPublishResourceInfo info = getResourceInfo(resource, published, permissions, locked);
        CmsPublishResource pubResource = resourceToBean(resource, info, pubList.contains(resource), related);
        return pubResource;
    }
}
