/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.sramp.ui.client.local.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.databinding.client.api.PropertyChangeEvent;
import org.jboss.errai.databinding.client.api.PropertyChangeHandler;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.sramp.ui.client.local.pages.details.AddCustomPropertyDialog;
import org.overlord.sramp.ui.client.local.pages.details.ClassifiersPanel;
import org.overlord.sramp.ui.client.local.pages.details.CustomPropertiesPanel;
import org.overlord.sramp.ui.client.local.pages.details.DeleteArtifactDialog;
import org.overlord.sramp.ui.client.local.pages.details.DescriptionInlineLabel;
import org.overlord.sramp.ui.client.local.pages.details.ModifyClassifiersDialog;
import org.overlord.sramp.ui.client.local.pages.details.RelationshipsTable;
import org.overlord.sramp.ui.client.local.pages.details.SourceEditor;
import org.overlord.sramp.ui.client.local.services.ArtifactRpcService;
import org.overlord.sramp.ui.client.local.services.NotificationService;
import org.overlord.sramp.ui.client.local.services.rpc.IRpcServiceInvocationHandler;
import org.overlord.sramp.ui.client.local.util.DOMUtil;
import org.overlord.sramp.ui.client.local.util.DataBindingDateConverter;
import org.overlord.sramp.ui.client.local.widgets.common.EditableInlineLabel;
import org.overlord.sramp.ui.client.local.widgets.common.HtmlSnippet;
import org.overlord.sramp.ui.client.shared.beans.ArtifactBean;
import org.overlord.sramp.ui.client.shared.beans.ArtifactRelationshipsBean;
import org.overlord.sramp.ui.client.shared.beans.NotificationBean;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;

/**
 * The page shown to the user when she clicks on one of the artifacts
 * displayed in the Artifacts Table on the Artifacts page.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/sramp/ui/client/local/site/artifact-details.html#page")
@Page(path="details")
@Dependent
public class ArtifactDetailsPage extends AbstractPage {

    @Inject
    protected ArtifactRpcService artifactService;
    @Inject
    protected NotificationService notificationService;
    protected ArtifactBean currentArtifact;

    @PageState
    private String uuid;

    @Inject @AutoBound
    protected DataBinder<ArtifactBean> artifact;

    // Breadcrumbs
    @Inject @DataField("back-to-dashboard")
    TransitionAnchor<DashboardPage> backToDashboard;
    @Inject @DataField("back-to-artifacts")
    TransitionAnchor<ArtifactsPage> backToArtifacts;

    // Actions
    @Inject  @DataField("btn-delete")
    Button deleteButton;
    @Inject
    DeleteArtifactDialog deleteDialog;

    // Overview tab
    @Inject @DataField("core-property-name") @Bound(property="name")
    EditableInlineLabel name;
    @Inject @DataField("core-property-version") @Bound(property="version")
    InlineLabel version;
    @Inject @DataField("core-property-type-1") @Bound(property="type")
    InlineLabel htype;
    @Inject @DataField("link-download-content")
    Anchor downloadContentLink;
    @Inject @DataField("link-download-metaData")
    Anchor downloadMetaDataLink;
    @Inject @DataField("core-property-type-2") @Bound(property="type")
    Label type;
    @Inject @DataField("core-property-uuid") @Bound(property="uuid")
    Label uuidField;
    @Inject @DataField("core-property-derived") @Bound(property="derived")
    Label derived;
    @Inject @DataField("core-property-createdOn") @Bound(property="createdOn", converter=DataBindingDateConverter.class)
    InlineLabel createdOn;
    @Inject @DataField("core-property-createdBy") @Bound(property="createdBy")
    InlineLabel createdBy;
    @Inject @DataField("core-property-modifiedOn") @Bound(property="updatedOn", converter=DataBindingDateConverter.class)
    InlineLabel modifiedOn;
    @Inject @DataField("core-property-modifiedBy") @Bound(property="updatedBy")
    InlineLabel modifiedBy;
    @Inject @DataField("custom-properties-container") @Bound(property="properties")
    CustomPropertiesPanel customProperties;
    @Inject @DataField("add-property-button")
    Anchor addProperty;
    @Inject
    Instance<AddCustomPropertyDialog> addPropertyDialogFactory;
    @Inject @DataField("classifiers-container") @Bound(property="classifiedBy")
    ClassifiersPanel classifiers;
    @Inject @DataField("modify-classifiers-button")
    Anchor modifyClassifier;
    @Inject
    Instance<ModifyClassifiersDialog> modifyClassifiersDialogFactory;
    @Inject @DataField("core-property-description") @Bound(property="description")
    DescriptionInlineLabel description;

    // Relationships tab
    @Inject @DataField("sramp-artifact-tabs-relationships")
    Anchor relationshipsTabAnchor;
    @Inject @DataField("relationship-tab-progress")
    HtmlSnippet relationshipsTabProgress;
    @Inject @DataField("relationships-table")
    RelationshipsTable relationships;
    protected boolean relationshipsLoaded;

    // Source tab
    @Inject @DataField("sramp-artifact-tabs-source")
    Anchor sourceTabAnchor;
    @Inject @DataField("source-tab-progress")
    HtmlSnippet sourceTabProgress;
    @Inject @DataField("artifact-editor")
    SourceEditor sourceEditor;
    protected boolean sourceLoaded;

    @Inject @DataField("artifact-details-loading-spinner")
    protected HtmlSnippet artifactLoading;
    protected Element pageContent;
    protected Element editorWrapper;

    /**
     * Constructor.
     */
    public ArtifactDetailsPage() {
    }

    /**
     * Called after the widget is constructed.
     */
    @PostConstruct
    protected void onPostConstruct() {
        pageContent = DOMUtil.findElementById(getElement(), "artifact-details-content-wrapper");
        editorWrapper = DOMUtil.findElementById(getElement(), "editor-wrapper");
        artifact.addPropertyChangeHandler(new PropertyChangeHandler<Object>() {
            @Override
            public void onPropertyChange(PropertyChangeEvent<Object> event) {
                pushModelToServer();
            }
        });
        deleteDialog.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onDeleteConfirm();
            }
        });
    }

    /**
     * @see org.overlord.sramp.ui.client.local.pages.AbstractPage#onPageShowing()
     */
    @Override
    protected void onPageShowing() {
        sourceLoaded = false;
        currentArtifact = null;
        pageContent.addClassName("hide");
        artifactLoading.getElement().removeClassName("hide");
        sourceTabAnchor.setVisible(false);
        editorWrapper.setAttribute("style", "display:none");
        artifactService.get(uuid, new IRpcServiceInvocationHandler<ArtifactBean>() {
            @Override
            public void onReturn(ArtifactBean data) {
                currentArtifact = data;
                updateArtifactMetaData(data);
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification("Error getting artifact details.", error);
            }
        });
        relationshipsTabAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (!relationshipsLoaded) {
                    loadRelationships(currentArtifact);
                }
            }
        });
        sourceTabAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (!sourceLoaded) {
                    loadSource(currentArtifact);
                }
            }
        });
    }

    /**
     * Called when the user clicks the Add Property button.
     * @param event
     */
    @EventHandler("add-property-button")
    protected void onAddProperty(ClickEvent event) {
        AddCustomPropertyDialog dialog = addPropertyDialogFactory.get();
        dialog.addValueChangeHandler(new ValueChangeHandler<Map.Entry<String,String>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Entry<String, String>> event) {
                Entry<String, String> value = event.getValue();
                if (value != null) {
                    String propName = value.getKey();
                    String propValue = value.getValue();
                    Map<String, String> newProps = new HashMap<String,String>(artifact.getModel().getProperties());
                    newProps.put(propName, propValue);
                    customProperties.setValue(newProps, true);
                }
            }
        });
        dialog.show();
    }

    /**
     * Called when the user clicks the Delete button.
     * @param event
     */
    @EventHandler("btn-delete")
    protected void onDeleteClick(ClickEvent event) {
        deleteDialog.setArtifactName(artifact.getModel().getName());
        deleteDialog.show();
    }

    /**
     * Called when the user confirms the artifact deletion.
     */
    protected void onDeleteConfirm() {
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                "Deleting Artifact", "Deleting artifact '" + artifact.getModel().getName() + "', please wait...");
        artifactService.delete(artifact.getModel(), new IRpcServiceInvocationHandler<Void>() {
            @Override
            public void onReturn(Void data) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        "Artifact Deleted",
                        "You have successfully deleted artifact '" + artifact.getModel().getName() + "'.");
                backToArtifacts.click();
            }
            @Override
            public void onError(Throwable error) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        "Error Deleting Artifact",
                        error);
            }
        });
    }

    /**
     * Called when the user clicks the Add Property button.
     * @param event
     */
    @EventHandler("modify-classifiers-button")
    protected void onModifyClassifiers(ClickEvent event) {
        ModifyClassifiersDialog dialog = modifyClassifiersDialogFactory.get();
        dialog.addValueChangeHandler(new ValueChangeHandler<List<String>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<String>> event) {
                List<String> value = event.getValue();
                if (value != null) {
                    List<String> newValue = new ArrayList<String>(value);
                    classifiers.setValue(newValue, true);
                }
            }
        });
        dialog.setValue(artifact.getModel().getClassifiedBy());
        dialog.show();
    }

    /**
     * Loads the artifact's relationships and displays them in the proper table.
     * @param artifact
     */
    protected void loadRelationships(ArtifactBean artifact) {
        relationships.setVisible(false);
        relationshipsTabProgress.setVisible(true);
        artifactService.getRelationships(artifact.getUuid(), artifact.getType(), new IRpcServiceInvocationHandler<ArtifactRelationshipsBean>() {
            @Override
            public void onReturn(ArtifactRelationshipsBean data) {
                relationships.setValue(data.getRelationships());
                relationshipsTabProgress.setVisible(false);
                relationships.setVisible(true);
                relationshipsLoaded = true;
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification("Error getting artifact relationships.", error);
            }
        });
    }

    /**
     * Loads the artifact's source (async) into the source tab's editor control.
     * @param artifact
     */
    protected void loadSource(ArtifactBean artifact) {
        if (!artifact.isTextDocument()) {
            return;
        }
        sourceTabProgress.setVisible(true);
        artifactService.getDocumentContent(artifact.getUuid(), artifact.getType(), new IRpcServiceInvocationHandler<String>() {
            @Override
            public void onReturn(String data) {
                sourceEditor.setValue(data);
                sourceTabProgress.setVisible(false);
                editorWrapper.removeAttribute("style");
                sourceLoaded = true;
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification("Error getting artifact content.", error);
            }
        });
    }

    /**
     * Called when the artifact meta data is loaded.
     * @param artifact
     */
    protected void updateArtifactMetaData(ArtifactBean artifact) {
        this.artifact.setModel(artifact, InitialState.FROM_MODEL);
        String contentUrl = GWT.getModuleBaseURL() + "services/artifactDownload";
        contentUrl += "?uuid=" + artifact.getUuid() + "&type=" + artifact.getType();
        String metaDataUrl = contentUrl + "&as=meta-data";
        this.downloadContentLink.setHref(contentUrl);
        this.downloadContentLink.setVisible(!artifact.isDerived());
        this.downloadMetaDataLink.setHref(metaDataUrl);
        this.sourceEditor.setValue("");

        if (artifact.isTextDocument()) {
            sourceTabAnchor.setVisible(true);
        }
        if (artifact.isDocument()) {
            this.downloadContentLink.getElement().removeClassName("hidden");
        } else {
            this.downloadContentLink.getElement().addClassName("hidden");
        }
        artifactLoading.getElement().addClassName("hide");
        pageContent.removeClassName("hide");
    }

    /**
     * Sends the model back up to the server (saves local changes).
     */
    // TODO i18n
    protected void pushModelToServer() {
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                "Updating Artifact", "Updating artifact '" + artifact.getModel().getName() + "', please wait...");
        artifactService.update(artifact.getModel(), new IRpcServiceInvocationHandler<Void>() {
            @Override
            public void onReturn(Void data) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        "Update Complete",
                        "You have successfully updated artifact '" + artifact.getModel().getName() + "'.");
            }
            @Override
            public void onError(Throwable error) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        "Error Updating Artifact",
                        error);
            }
        });
    }

}
