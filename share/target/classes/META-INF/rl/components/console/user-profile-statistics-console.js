/**
 * RL root namespace.
 * 
 * @namespace RL
 */
// Ensure RL root object exists
if (typeof RL == "undefined" || !RL) {
  var RL = {};
}
/**
 * Admin Console User Statistics Console
 * 
 * @namespace Alfresco
 * @class RL.SiteStatisticsConsole
 */
(function() {
  /**
   * YUI Library aliases
   */
  var Dom = YAHOO.util.Dom;
  var $html = Alfresco.util.encodeHTML;
  var Bubbling = YAHOO.Bubbling;
  /**
   * UserProfileStatisticsConsole constructor.
   * 
   * @param {String}
   *            htmlId The HTML id of the parent element
   * @return {RL.UserProfileStatisticsConsole} The new UserProfileStatisticsConsole instance
   * @constructor
   */
  RL.UserProfileStatisticsConsole = function(htmlId) {
    this.name = "RL.UserProfileStatisticsConsole";
    RL.UserProfileStatisticsConsole.superclass.constructor.call(this, htmlId);
    /* Register this component */
    Alfresco.util.ComponentManager.register(this);
    /* Load YUI Components */
    Alfresco.util.YUILoaderHelper.require(["button", "container", "datasource", "datatable", "paginator", "json", "history"], this.onComponentsLoaded, this);
    /* Define panel handlers */
    var parent = this;
    this.data = {
      "selectionData": new Object(),
      "userProfileData": new Object(),
      "summaryData": new Object()
    };
    // loading message function
    this.loadingMessage = null;
    this.fnShowLoadingMessage = function() {
      parent.loadingMessage = Alfresco.util.PopupManager.displayMessage({
        displayTime: 0,
        text: '<span class="wait">' + $html(parent.msg("message.loading")) + '</span>',
        noEscape: true
      });
    };
    // slow data webscript message
    this.timerShowLoadingMessage = null;
    /* File List Panel Handler */
    ListPanelHandler = function ListPanelHandler_constructor() {
      ListPanelHandler.superclass.constructor.call(this, "main");
    };
    this.LoadAvailableYears = function() {
      var selectBox = Dom.get(this.id + "-statistics-control-year");
      selectBox.onchange = this.OnChangeYearFilter;
      this.timerShowLoadingMessage = YAHOO.lang.later(2000, this, this.fnShowLoadingMessage);
      Alfresco.util.Ajax.request({
        url: Alfresco.constants.PROXY_URI + "slingshot/doclib/treenode/node/alfresco/company/home/Data%20Dictionary/Redpill-Linpro/Statistics/UserProfileStatistics/json",
        responseContentType: Alfresco.util.Ajax.JSON,
        method: Alfresco.util.Ajax.GET,
        successCallback: {
          fn: this.LoadAvailableYears_success,
          scope: this
        },
        failureCallback: {
          fn: this.LoadAvailableYears_failure,
          scope: this
        }
      });
    };
    this.LoadAvailableYears_success = function(response) {
      if (this.timerShowLoadingMessage) {
        this.timerShowLoadingMessage.cancel();
      }
      if (this.loadingMessage) {
        try {
          this.loadingMessage.destroy();
        } catch (err) {

        }
      }
      if (response != null && response.json != null) {
        var items = response.json.items;
        var selectBox = Dom.get(this.id + "-statistics-control-year");
        for (var i = 0; i < items.length; i++) {
          var item = items[i];
          var option = document.createElement("option");
          option.value = item.name;
          option.innerHTML = item.name;
          selectBox.appendChild(option);
        }
      }
    };
    this.LoadAvailableYears_failure = function(response) {
      if (this.timerShowLoadingMessage) {
        this.timerShowLoadingMessage.cancel();
      }
      if (this.loadingMessage) {
        try {
          this.loadingMessage.destroy();
        } catch (err) {

        }
      }
    };
    this.OnChangeYearFilter = function(evt) {
      var option = evt.currentTarget.options[evt.currentTarget.selectedIndex];
      var target = option.value;
      parent.timerShowLoadingMessage = YAHOO.lang.later(2000, this, parent.fnShowLoadingMessage);
      Alfresco.util.Ajax.request({
        url: Alfresco.constants.PROXY_URI + "/slingshot/doclib2/doclist/all/node/alfresco/company/home/Data%20Dictionary/Redpill-Linpro/Statistics/UserProfileStatistics/json/" + target + "?sortField=cm%3acreated&sortAsc=false",
        responseContentType: Alfresco.util.Ajax.JSON,
        method: Alfresco.util.Ajax.GET,
        successCallback: {
          fn: parent.OnChangeYearFilter_success,
          scope: parent
        },
        failureCallback: {
          fn: parent.OnChangeYearFilter_failure,
          scope: parent
        }
      });
    };
    this.OnChangeYearFilter_success = function(response) {
      if (parent.timerShowLoadingMessage) {
        parent.timerShowLoadingMessage.cancel();
      }
      if (parent.loadingMessage) {
        try {
          this.loadingMessage.destroy();
        } catch (err) {

        }
      }
      if (response != null && response.json != null) {
        parent.data.selectionData = response.json;
        var successHandlerSelection = function(request, response, payload) {
          parent.widgets.dataTableSelection.onDataReturnInitializeTable.call(parent.widgets.dataTableSelection, request, response, payload);
        };
        var oCallbackSelection = {
          success: successHandlerSelection,
          failure: successHandlerSelection,
          scope: this.widgets.dataTableSelection,
          argument: this.widgets.dataTableSelection.getState()
        };
        this.widgets.dataSourceSelection.sendRequest("", oCallbackSelection);
      }
    };
    this.OnChangeYearFilter_failure = function(response) {
      if (parent.timerShowLoadingMessage) {
        parent.timerShowLoadingMessage.cancel();
      }
      if (parent.loadingMessage) {
        try {
          this.loadingMessage.destroy();
        } catch (err) {

        }
      }
    };
    this.SelectStatisticsRow = function(evt) {
      var nodeRef = this.getRecord(evt.target).getData().node.nodeRef;
      var nodeRefStr = nodeRef.replace("://", "/");
      parent.timerShowLoadingMessage = YAHOO.lang.later(2000, parent, parent.fnShowLoadingMessage);
      Alfresco.util.Ajax.request({
        url: Alfresco.constants.PROXY_URI + "api/node/" + nodeRefStr + "/content",
        responseContentType: Alfresco.util.Ajax.JSON,
        method: Alfresco.util.Ajax.GET,
        successCallback: {
          fn: parent.SelectStatisticsRow_success,
          scope: parent
        },
        failureCallback: {
          fn: parent.SelectStatisticsRow_failure,
          scope: parent
        }
      });
    };
    this.SelectStatisticsRow_success = function(response) {
      if (parent.timerShowLoadingMessage) {
        parent.timerShowLoadingMessage.cancel();
      }
      if (parent.loadingMessage) {
        try {
          this.loadingMessage.destroy();
        } catch (err) {

        }
      }
      if (response != null) {
//        this.data.userProfileData.UserStats = response.json.UserStats;
//        this.data.userProfileData.startIndex = 0;
//        this.data.userProfileData.totalRecords = response.json.UserStats.length;
        this.data.summaryData.Summary = response.json.Summary;
        this.data.summaryData.startIndex = 0;
        this.data.summaryData.totalRecords = response.json.Summary.length;
//        var successHandlerInternal = function(request, response, payload) {
//          parent.widgets.dataTableSite.onDataReturnInitializeTable.call(parent.widgets.dataTableSite, request, response, payload);
//        };
        var successHandlerSummary = function(request, response, payload) {
          parent.widgets.dataTableSummary.onDataReturnInitializeTable.call(parent.widgets.dataTableSummary, request, response, payload);
        };
//        var oCallbackInternal = {
//          success: successHandlerInternal,
//          failure: successHandlerInternal,
//          scope: this.widgets.dataTableSite,
//          argument: this.widgets.dataTableSite.getState()
//        };
//        this.widgets.dataSourceSite.sendRequest("", oCallbackInternal);
        var oCallbackSummary = {
            success: successHandlerSummary,
            failure: successHandlerSummary,
            scope: this.widgets.dataTableSummary,
            argument: this.widgets.dataTableSummary.getState()
          };
          this.widgets.dataSourceSummary.sendRequest("", oCallbackSummary);
      }
    };
    this.SelectStatisticsRow_failure = function(response) {
      if (parent.timerShowLoadingMessage) {
        parent.timerShowLoadingMessage.cancel();
      }
      if (parent.loadingMessage) {
        try {
          this.loadingMessage.destroy();
        } catch (err) {

        }
      }
    };
    YAHOO.extend(ListPanelHandler, Alfresco.ConsolePanelHandler, {
      /**
       * Called by YAHOO.lang.JSON.stringify(the
       * ConsolePanelHandler when this panel shall be
       * loaded
       * 
       * @method onLoad
       */
      onLoad: function onLoad() {
        // DataTable and DataSource setup
        parent.widgets.dataSourceSelection = new YAHOO.util.FunctionDataSource(function() {
          return YAHOO.lang.JSON.stringify(parent.data.selectionData);
        }, {
          "responseType": YAHOO.util.FunctionDataSource.TYPE_JSON,
          responseSchema: {
            resultsList: "items",
            metaFields: {
              recordOffset: "startIndex",
              totalRecords: "totalRecords"
            }
          }
        });
        // DataTable and DataSource setup
//        parent.widgets.dataSourceSite = new YAHOO.util.FunctionDataSource(function() {
//          return YAHOO.lang.JSON.stringify(parent.data.userProfileData);
//        }, {
//          "responseType": YAHOO.util.FunctionDataSource.TYPE_JSON,
//          responseSchema: {
//            resultsList: "UserStats",
//            metaFields: {
//              recordOffset: "startIndex",
//              totalRecords: "totalRecords"
//            }
//          }
//        });
        parent.widgets.dataSourceSummary = new YAHOO.util.FunctionDataSource(function() {
          return YAHOO.lang.JSON.stringify(parent.data.summaryData);
        }, {
          "responseType": YAHOO.util.FunctionDataSource.TYPE_JSON,
          responseSchema: {
            resultsList: "Summary",
            metaFields: {
              recordOffset: "startIndex",
              totalRecords: "totalRecords"
            }
          }
        });
        // Setup the main datatable
        this._setupDataSelectionTable();
        this._setupDataStatsTables();
        parent.LoadAvailableYears();
        //parent.LoadUserActivity();
      },
      _setupDataSelectionTable: function() {
        
        var renderCellDate = function(cell, record, column, data) {
          
          cell.innerHTML = $html(record.getData().node.properties["cm:created"].value);
        };
        var renderCellLink = function(cell, record, column, data) {
          var filename = record.getData().location.file;
          var path = record.getData().location.path;
          var year = path.substring(path.length -4);
          var fn = filename.replace("json", "csv");
          var go = "<a class='userprofiledownloadstats' title='Download CSV' " + "href='https://teamroom.scania.com/alfresco/s/com/scania/alfresco/teamroom/repo/getCSVcontentStats.csv?content="+fn+"|"+year+"'>csv link</a>";
          //"+Alfresco.constants.URL_SERVICECONTEXT+"
          cell.innerHTML += go;
        };
        var columnDefinitions = [{
          key: "node.properties.cm:created",
          label: parent._msg("select-statistics.label"),
          sortable: false,
          formatter: renderCellDate
        }, {
          key: "node.properties.cm:created",
          label: parent._msg("select-download.label"),
          sortable: false,
          formatter: renderCellLink
        }];

        parent.widgets.dataTableSelection = new YAHOO.widget.DataTable(parent.id + "-statistics-select-list", columnDefinitions, parent.widgets.dataSourceSelection, {
          MSG_EMPTY: parent._msg("message.emptystatistics"),
          MSG_ERROR: parent._msg("message.emptystatistics"),
          selectionMode: "single"
        });
        parent.widgets.dataTableSelection.subscribe("rowMouseoverEvent", parent.widgets.dataTableSelection.onEventHighlightRow);
        parent.widgets.dataTableSelection.subscribe("rowMouseoutEvent", parent.widgets.dataTableSelection.onEventUnhighlightRow);
        parent.widgets.dataTableSelection.subscribe("rowClickEvent", parent.widgets.dataTableSelection.onEventSelectRow);
        parent.widgets.dataTableSelection.subscribe("rowClickEvent", parent.SelectStatisticsRow);
      },
      _setupDataStatsTables: function() {
        var renderCellUserName = function(cell, record, column, data) {
          cell.innerHTML = $html(data);
        };
        var renderCellLastUpdated = function(cell, record, column, data) {
          cell.innerHTML = $html(data);
        };
        var renderCellPictureFilename = function(cell, record, column, data) {
          cell.innerHTML = $html(data);
        };
        var renderCellNickName = function(cell, record, column, data) {
          cell.innerHTML = $html(data);
        };
        var renderCellJobTitle = function(cell, record, column, data) {
          cell.innerHTML = $html(data);
        };
        var renderCellOfficePhone = function(cell, record, column, data) {
          cell.innerHTML = $html(data);
        };
        var renderCellMobilePhone = function(cell, record, column, data) {
          cell.innerHTML = $html(data);
        };
        var renderCellGeoLocation = function(cell, record, column, data) {
          cell.innerHTML = $html(data);
        };
        var renderCellCompany = function(cell, record, column, data) {
          cell.innerHTML = $html(data);
        };
        var renderCellAbout = function(cell, record, column, data) {
          cell.innerHTML = $html(data);
        };
        var renderCellLocalIntranet = function(cell, record, column, data) {
          cell.innerHTML = $html(data);
        };
        var renderCellSkills = function(cell, record, column, data) {
          cell.innerHTML = $html(data);
        };
        var renderCellNumUsers = function(cell, record, column, data) {
          cell.innerHTML = $html(data[0]);
        };
        var renderCellNumUpdated = function(cell, record, column, data) {
          cell.innerHTML = $html(data[1]);
        };
        var renderCellNumPictures = function(cell, record, column, data) {
          cell.innerHTML = $html(data[2]);
        };
        var renderCellNumNickName = function(cell, record, column, data) {
          cell.innerHTML = $html(data[3]);
        };
        var renderCellNumJobTitle = function(cell, record, column, data) {
          cell.innerHTML = $html(data[4]);
        };
        var renderCellNumOfficePhone = function(cell, record, column, data) {
          cell.innerHTML = $html(data[5]);
        };
        var renderCellNumMobilePhone = function(cell, record, column, data) {
          cell.innerHTML = $html(data[6]);
        };
        var renderCellNumGeoLocation = function(cell, record, column, data) {
          cell.innerHTML = $html(data[7]);
        };
        var renderCellNumCompany = function(cell, record, column, data) {
          cell.innerHTML = $html(data[8]);
        };
        var renderCellNumAbout = function(cell, record, column, data) {
          cell.innerHTML = $html(data[9]);
        };
        var renderCellNumLocalIntranet = function(cell, record, column, data) {
          cell.innerHTML = $html(data[10]);
        };
        var renderCellNumSkills = function(cell, record, column, data) {
          cell.innerHTML = $html(data[11]);
        };

//        var columnDefinitions = [{
//          key: "userName",
//          label: parent._msg("label.userName"),
//          sortable: true,
//          formatter: renderCellUserName,
//          width: 110
//        }, {
//          key: "updated",
//          label: parent._msg("label.lastUpdated"),
//          sortable: true,
//          formatter: renderCellLastUpdated
//        }, {
//          key: "pictureFilename",
//          label: parent._msg("label.pictureFilename"),
//          sortable: true,
//          formatter: renderCellPictureFilename
//        }, {
//          key: "nickName",
//          label: parent._msg("label.nickName"),
//          sortable: true,
//          formatter: renderCellNickName
//        }, {
//          key: "jobTitle",
//          label: parent._msg("label.jobTitle"),
//          sortable: true,
//          formatter: renderCellJobTitle
//        }, {
//          key: "officePhone",
//          label: parent._msg("label.officePhone"),
//          sortable: true,
//          formatter: renderCellOfficePhone
//        }, {
//          key: "mobilePhone",
//          label: parent._msg("label.mobilePhone"),
//          sortable: true,
//          formatter: renderCellMobilePhone
//        }, {
//          key: "location",
//          label: parent._msg("label.geoLocation"),
//          sortable: true,
//          formatter: renderCellGeoLocation
//        }, {
//          key: "company",
//          label: parent._msg("label.company"),
//          sortable: true,
//          formatter: renderCellCompany
//        }, {
//          key: "about",
//          label: parent._msg("label.about"),
//          sortable: true,
//          formatter: renderCellAbout
//        }, {
//          key: "localIntranet",
//          label: parent._msg("label.localIntranet"),
//          sortable: true,
//          formatter: renderCellLocalIntranet
//        }, {
//          key: "skills",
//          label: parent._msg("label.skills"),
//          sortable: true,
//          formatter: renderCellSkills
//        }];
        var columnDefinitionsSummary = [{
          key: "summary",
          label: parent._msg("label.numUsers"),
          sortable: true,
          formatter: renderCellNumUsers,
          width: 90
        }, {
          key: "summary",
          label: parent._msg("label.numUpdated"),
          sortable: true,
          formatter: renderCellNumUpdated
        }, {
          key: "summary",
          label: parent._msg("label.numPictures"),
          sortable: true,
          formatter: renderCellNumPictures
        }, {
          key: "summary",
          label: parent._msg("label.numNickName"),
          sortable: true,
          formatter: renderCellNumNickName
        }, {
          key: "summary",
          label: parent._msg("label.numJobTitle"),
          sortable: true,
          formatter: renderCellNumJobTitle
        }, {
          key: "summary",
          label: parent._msg("label.numOfficePhone"),
          sortable: true,
          formatter: renderCellNumOfficePhone
        }, {
          key: "summary",
          label: parent._msg("label.numMobilePhone"),
          sortable: true,
          formatter: renderCellNumMobilePhone
        }, {
          key: "summary",
          label: parent._msg("label.numGeoLocation"),
          sortable: true,
          formatter: renderCellNumGeoLocation
        }, {
          key: "summary",
          label: parent._msg("label.numCompany"),
          sortable: true,
          formatter: renderCellNumCompany
        }, {
          key: "summary",
          label: parent._msg("label.numAbout"),
          sortable: true,
          formatter: renderCellNumAbout
        }, {
          key: "summary",
          label: parent._msg("label.numLocalIntranet"),
          sortable: true,
          formatter: renderCellNumLocalIntranet
        }, {
          key: "summary",
          label: parent._msg("label.numSkills"),
          sortable: true,
          formatter: renderCellNumSkills
        }];
        parent.widgets.dataTableSummary = new YAHOO.widget.DataTable(parent.id + "-statistics-UPsummary-list", columnDefinitionsSummary, parent.widgets.dataSourceSummary, {
          MSG_EMPTY: parent._msg("message.empty"),
          MSG_ERROR: parent._msg("message.empty")
        });
//        parent.widgets.dataTableSite = new YAHOO.widget.DataTable(parent.id + "-statistics-users-list", columnDefinitions, parent.widgets.dataSourceSite, {
//          MSG_EMPTY: parent._msg("message.empty"),
//          MSG_ERROR: parent._msg("message.empty")
//        });
      }
    });
    new ListPanelHandler();
    return this;
  };
  YAHOO.extend(RL.UserProfileStatisticsConsole, Alfresco.ConsoleTool, {
    /**
     * Fired by YUI when parent element is available for scripting.
     * Component initialisation, including instantiation of YUI widgets and
     * event listener binding.
     * 
     * @method onReady
     */
    onReady: function() {
      var self = this;
      // Call super-class onReady() method
      RL.UserProfileStatisticsConsole.superclass.onReady.call(this);
    },
    onRefreshClick: function() {
      this.LoadUserActivity();
    },
    /**
     * Gets a custom message
     * 
     * @method _msg
     * @param messageId
     *            {string} The messageId to retrieve
     * @return {string} The custom message
     * @private
     */
    _msg: function(messageId) {
      return Alfresco.util.message.call(this, messageId, "RL.UserProfileStatisticsConsole", Array.prototype.slice.call(arguments).slice(1));
    },
    /**
     * Resets the YUI DataTable errors to our custom messages
     * 
     * NOTE: Scope could be YAHOO.widget.DataTable, so can't use "this"
     * 
     * @method _setDefaultDataTableErrors
     * @param dataTable
     *            {object} Instance of the DataTable
     */
    _setDefaultDataTableErrors: function(dataTable) {
      var msg = Alfresco.util.message;
      dataTable.set("MSG_EMPTY", msg("message.empty", "RL.UserProfileStatisticsConsole"));
      dataTable.set("MSG_ERROR", msg("message.error", "RL.UserProfileStatisticsConsole"));
    }
  });
})();