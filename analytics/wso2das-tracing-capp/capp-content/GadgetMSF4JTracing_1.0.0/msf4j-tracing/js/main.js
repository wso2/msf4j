gadgets.util.registerOnLoadHandler(function () {
    gadgets.window.adjustHeight();
});

$(document).ready(function () {
    var prefs = new _IG_Prefs();

    var traceGroupsElm = $('#trace-groups');

    var traceTreeVwElm = $("#trace-tree-view");
    var traceTreeVw = initTraceTreeView(
        traceTreeVwElm,
        traceTreeVwElm.find(".container").first(),
        traceTreeVwElm.find(".close").first()
    );

    (function () {
        var refreshTracesElm = $("#refresh-traces");
        var isLoading = false;

        load();

        function load() {
            if (isLoading == false) {
                isLoading = true;
                loadAndRenderTraceGroups(prefs, traceGroupsElm, traceTreeVw, function () {
                    isLoading = false;
                });
            }
        }

        refreshTracesElm.click(function () {
            load();
        });
    })();
});

function loadAndRenderTraceGroups(prefs, traceGroupsElm, traceTreeVw, callback) {

    var analyticsPath = "/analytics/tables/MSF4J-TRACING";

    var timeBack = prefs.getInt("timeBack");
    var dasHost = prefs.getString("dasHost");
    var username = prefs.getString("username");
    var password = prefs.getString("password");

    loadTraces((function (dasHost, analyticsPath, timeBack) {
        return dasHost + analyticsPath +
            ((dasHost.substr(dasHost.length - 1) == "/") ? "" : "/") +
            ((new Date()).getTime() - timeBack * 60 * 1000);
    })(dasHost, analyticsPath, timeBack), timeBack, username, password, function (data) {
        var traceGroups = groupTraceEvents(data);
        renderTraceGroups(traceGroups, traceGroupsElm, traceTreeVw);
        callback();
    }, function (jqXHR) {
        callback();
        var errorData = $.parseJSON(jqXHR.responseText);
        if (errorData) {
            alert(errorData.message);
        } else {
            alert("Failed to load traces");
        }
    });

}

function initBarInfoView(rootElm, traceData, closeCallBack) {

    var close = null;

    function buildBarInfoVwModel(traceData) {
        var vwModel = {
            chttpMethod: "-",
            shttpMethod: "-",
            clientPath: "-",
            serverPath: "-",
            cStatusCode: "-",
            sStatusCode: "-",
            clientStartTime: "-",
            serverStartTime: "-",
            serverEndTime: "-",
            clientEndTime: "-",
            serverInstName: "-",
            clientInstName: "-",
            serverInstId: "-",
            clientInstId: "-"
        };
        if (traceData.type == "CTS") {
            vwModel.chttpMethod = traceData.httpMethod || "-";
            vwModel.clientPath = traceData.url || "-";
            vwModel.clientStartTime = traceData.timeStr || "-";
            vwModel.clientEndTime = (traceData.end) ? traceData.end.timeStr || "-" : "-";
            vwModel.clientInstName = traceData.clientName || "-";
            vwModel.clientInstId = traceData.instanceId || "-";
            vwModel.cStatusCode = (traceData.end) ? traceData.end.statusCode : "-";
            if (traceData.service) {
                vwModel.shttpMethod = traceData.service.httpMethod || "-";
                vwModel.serverPath = traceData.service.url || "-";
                vwModel.serverStartTime = traceData.service.timeStr || "-";
                vwModel.serverEndTime = (traceData.service.end) ? traceData.service.end.timeStr || "-" : "-";
                vwModel.serverInstName = traceData.instanceName || "-";
                vwModel.serverInstId = traceData.service.instanceId || "-";
                vwModel.sStatusCode = (traceData.service.end) ? traceData.service.end.statusCode : "-";
            }
        } else if (traceData.type == "STS") {
            vwModel.shttpMethod = traceData.httpMethod || "-";
            vwModel.serverPath = traceData.url || "-";
            vwModel.serverStartTime = traceData.timeStr || "-";
            vwModel.serverEndTime = (traceData.end) ? traceData.end.timeStr || "-" : "-";
            vwModel.serverInstName = traceData.instanceName || "-";
            vwModel.serverInstId = traceData.instanceId || "-";
            vwModel.sStatusCode = (traceData.end) ? traceData.end.statusCode : "-";
        }
        return vwModel;
    }

    function show(traceData) {
        var barInfoElm = $(
            Mustache.render(
                $('#bar-info').html(),
                buildBarInfoVwModel(traceData)
            )
        );

        close = function () {
            barInfoElm.remove();
            if (closeCallBack) {
                closeCallBack();
            }
        };

        // var closeBtnElm = barInfoElm.find(".close").first();
        // closeBtnElm.click(close);

        rootElm.append(barInfoElm);
    }

    return {
        show: show,
        close: function () {
            if (close) {
                close();
            }
        }
    };
}

function initTraceTreeView(viewElm, containerElm, closeBtnElm) {

    hide();

    closeBtnElm.click(function () {
        hide();
    });

    function clean() {
        containerElm.empty();
    }

    function hide() {
        clean();
        viewElm.hide();
    }

    function renderTimeLine(start, end) {
        var timeLineElm = $("#time-line");
        var timeGaps = [
            {
                gap: 1,
                unit: "ms",
                unitMs: 1
            },
            {
                gap: 5,
                unit: "ms",
                unitMs: 1
            },
            {
                gap: 10,
                unit: "ms",
                unitMs: 1
            },
            {
                gap: 100,
                unit: "s",
                unitMs: 1000
            },
            {
                gap: 200,
                unit: "s",
                unitMs: 1000
            },
            {
                gap: 500,
                unit: "s",
                unitMs: 1000
            },
            {
                gap: 1000,
                unit: "s",
                unitMs: 1000
            },
            {
                gap: 1000 * 5,
                unit: "s",
                unitMs: 1000
            },
            {
                gap: 1000 * 10,
                unit: "s",
                unitMs: 1000
            },
            {
                gap: 1000 * 60,
                unit: "s",
                unitMs: 1000 * 60
            },
            {
                gap: 1000 * 60 * 5,
                unit: "m",
                unitMs: 1000 * 60
            },
            {
                gap: 1000 * 60 * 10,
                unit: "m",
                unitMs: 1000 * 60
            },
            {
                gap: 1000 * 60 * 60,
                unit: "h",
                unitMs: 1000 * 60 * 60
            }
        ];
        var timeDiff = end - start;

        function render() {
            var spaceDiff = timeLineElm.width();
            var minGapWidth = 40;
            var timeGap = (function () {
                var timeGapsLen = timeGaps.length;
                for (var i = 0; i < timeGapsLen; i++) {
                    var timeGap = timeGaps[i];
                    var gapCount = (timeDiff / timeGap.gap);
                    var gapWidth = spaceDiff / gapCount;
                    if (gapWidth >= minGapWidth) {
                        return timeGap;
                    }
                }
                return timeGaps[timeGapsLen - 1];
            })();
            var gapCount = (timeDiff / timeGap.gap);
            var unitGap = timeGap.gap / timeGap.unitMs;
            for (var i = 0; i < gapCount; i++) {
                var left = (i * timeGap.gap) * 100 / timeDiff;
                var unitElm = $(document.createElement("div"));
                unitElm.attr("class", "unit");
                unitElm.css("left", left + "%");
                unitElm.html((i * unitGap).toFixed(3) + timeGap.unit);
                timeLineElm.append(unitElm);
            }
        }

        timeLineElm.empty();
        render();
        timeLineElm.resize(function () {
            timeLineElm.empty();
            render();
        });
    }

    function show(traceTree) {
        viewElm.show();
        renderTraceTree(traceTree, buildOriginTraceBarVw(traceTree, traceTree.timeRange, 0));
        renderTimeLine(traceTree.timeRange.start, traceTree.timeRange.end);
    }

    function buildOriginTraceBarVw(traceTree, timeRange, callDepth) {
        var originTraceBarVw = buildTraceBarView(traceTree, timeRange, callDepth);
        containerElm.append(originTraceBarVw.traceBarElm);
        return originTraceBarVw;
    }

    function renderTraceTree(traceTree, parentTraceBarVw) {
        var children = traceTree.children;
        var childrenLen = children.length;
        for (var i = 0; i < childrenLen; i++) {
            var child = children[i];
            var childTraceBarVw = buildTraceBarView(
                child,
                parentTraceBarVw.timeRange,
                // Do not indent the service call of a client
                (child.type == "STS") ? parentTraceBarVw.callDepth : parentTraceBarVw.callDepth + 1
            );
            parentTraceBarVw.addChildTraceBar(childTraceBarVw);
            renderTraceTree(child, childTraceBarVw);
        }
    }

    return {
        show: function (traceTree) {
            show(traceTree);
        }
    };
}

function buildTraceBarView(traceData, timeRange, callDepth) {
    var traceBarElm = $(
        Mustache.render(
            $('#trace-bar').html()
        )
    );
    var childContainerElm = traceBarElm.find(".child-container").first();
    var leftPaneElm = traceBarElm.find(".left-pane").first();
    var arrowElm = traceBarElm.find(".arrow").first();
    var labelElm = traceBarElm.find(".label").first();
    var barElm = traceBarElm.find(".bar").first();
    var barInfoContElm = traceBarElm.find(".bar-info-container").first();
    var childTraceBarVws = [];
    var collapseChildren = null;

    setName(traceData.instanceName);
    setTimeRange(
        traceData.time,
        // If end time is not available, draw the bar to touch right end
        (traceData.end) ? traceData.end.time : timeRange.end,
        timeRange.start,
        timeRange.end
    );
    setCallDepth(callDepth);


    (function () {
        var inforBarVw = null;
        barElm.click(function () {
            if (!inforBarVw) {
                inforBarVw = initBarInfoView(barInfoContElm, traceData);
                inforBarVw.show(traceData);
            } else {
                inforBarVw.close();
                inforBarVw = null;
            }
        });
    })();

    (function (childTraceBarVws) {
        var isHidden = false;

        collapseChildren = function () {
            if (traceData.children.length > 0) {
                childContainerElm.hide();
                labelElm.css("font-weight", "bold");
                arrowElm.attr("class", "arrow-down");
                var childTraceBarVwsLen = childTraceBarVws.length;
                for (var i = 0; i < childTraceBarVwsLen; i++) {
                    childTraceBarVw = childTraceBarVws[i];
                    childTraceBarVw.collapseChildren();
                }
                isHidden = true;
            }
        };

        if (traceData.children.length > 0) {
            leftPaneElm.click(function () {
                if (isHidden) {
                    childContainerElm.show();
                    labelElm.css("font-weight", "normal");
                    arrowElm.attr("class", "arrow");
                    isHidden = false;
                } else {
                    collapseChildren();
                }
            });
        } else {
            arrowElm.attr("class", "arrow-hide");
        }
    })(childTraceBarVws);

    function setTimeRange(startTime, endTime, rootStart, rootEnd) {
        var left = 100 * (startTime - rootStart) / (rootEnd - rootStart);
        var width = 100 * (endTime - startTime) / (rootEnd - rootStart);
        barElm.css({
            left: left + "%",
            width: width + "%",
            "background-color": (function shadeBlend(p, c0, c1) {
                var n = p < 0 ? p * -1 : p, u = Math.round, w = parseInt;
                if (c0.length > 7) {
                    var f = c0.split(","), t = (c1 ? c1 : p < 0 ? "rgb(0,0,0)" : "rgb(255,255,255)").split(","), R = w(f[0].slice(4)), G = w(f[1]), B = w(f[2]);
                    return "rgb(" + (u((w(t[0].slice(4)) - R) * n) + R) + "," + (u((w(t[1]) - G) * n) + G) + "," + (u((w(t[2]) - B) * n) + B) + ")"
                } else {
                    var f = w(c0.slice(1), 16), t = w((c1 ? c1 : p < 0 ? "#000000" : "#FFFFFF").slice(1), 16), R1 = f >> 16, G1 = f >> 8 & 0x00FF, B1 = f & 0x0000FF;
                    return "#" + (0x1000000 + (u(((t >> 16) - R1) * n) + R1) * 0x10000 + (u(((t >> 8 & 0x00FF) - G1) * n) + G1) * 0x100 + (u(((t & 0x0000FF) - B1) * n) + B1)).toString(16).slice(1)
                }
            })((function () {
                var p = callDepth * 0.1;
                return (p > 0.4) ? 0.4 : p;
            })(callDepth), "#005FB0")
        });
    }

    function setName(name) {
        labelElm.html(name);
    }

    function addChildTraceBar(traceBarVw) {
        childTraceBarVws.push(traceBarVw);
        childContainerElm.append(traceBarVw.traceBarElm);
    }

    function setCallDepth(callDepth) {
        leftPaneElm.css("margin-left", callDepth * 15);
    }

    return {
        traceBarElm: traceBarElm,
        collapseChildren: collapseChildren,
        timeRange: timeRange,
        callDepth: callDepth,
        addChildTraceBar: function (traceBarVw) {
            addChildTraceBar(traceBarVw);
        }
    };
}

function loadTraces(url, timeBack, username, password, callback, errorCallback) {
    $.ajax({
        type: "GET",
        beforeSend: function (request) {
            if (!window.btoa) {
                window.btoa = $.base64.btoa;
            }
            request.setRequestHeader("Authorization", "Basic " + btoa(username + ":" + password));
        },
        url: url,
        data: "json",
        processData: false,
        success: callback,
        error: errorCallback
    });
}

function renderTraceGroups(traceGroups, rootElm, traceTreeVw) {
    rootElm.empty();
    itrValidTraceGroups(traceGroups, function (traceGroup) {
        var traceGroupElm = $(
            Mustache.render(
                $('#trace-group').html(),
                traceGroup
            )
        );
        traceGroupElm.click(function () {
            traceTreeVw.show(traceGroup.getTraceTree());
        });
        rootElm.prepend(traceGroupElm);
    });
}

function groupTraceEvents(data) {
    var traceGroups = {};
    var dataLen = data.length;
    for (var i = 0; i < dataLen; i++) {
        var event = data[i].values;
        if (!event || !event.originId) {
            // Ignore the event if it is null or does not contain the original event ID
            // Or the original event Id is invalid
            continue;
        }
        var eventDate = new Date(event.time);
        event.timeStr = eventDate.toTimeString()
            .replace(" GMT", ":" + ("00" + eventDate.getMilliseconds()).slice(-3) + " GMT");
        var traceGroup = traceGroups[event.originId];
        if (!traceGroup) {
            traceGroups[event.originId] = traceGroup = {
                isBuilt: false,
                timeRange: {
                    // The time range that the trace spans
                    // Required for drawing gantt bars to scale
                    start: Number.MAX_VALUE,
                    end: 0,
                    addStart: function (startTime) {
                        if (this.start > startTime) {
                            this.start = startTime;
                        }
                    },
                    addEnd: function (endTime) {
                        if (this.end < endTime) {
                            this.end = endTime;
                        }
                    }
                },
                origin: null,
                events: {
                    start: {},
                    end: {}
                },
                addEvent: function (event) {
                    if (event.type == "STS" || event.type == "CTS") {
                        if (event.traceId == event.originId) {
                            // If this condition is met this is the first event of the trace
                            if (validateOriginEvent(event)) {
                                this.origin = event;
                                this.timeRange.addStart(event.time);
                            }
                        } else {
                            var children = this.events.start[event.parentId];
                            if (!children) {
                                this.events.start[event.parentId] = children = [];
                            }
                            children.push(event);
                            this.timeRange.addStart(event.time);
                        }
                    } else {
                        this.events.end[event.traceId] = event;
                        this.timeRange.addEnd(event.time);
                    }
                },
                getTraceTree: function () {
                    if (this.isBuilt) {
                        return this.origin;
                    } else {
                        var startEvents = this.events.start;
                        var endEvents = this.events.end;
                        var origin = this.origin;
                        origin.timeRange = this.timeRange;
                        var parents = [];
                        parents.push(origin);
                        while (Object.keys(startEvents).length > 0 || parents.length > 0) {
                            var parent = parents.shift();
                            parent.end = endEvents[parent.traceId];
                            parent.children = startEvents[parent.traceId] || [];
                            delete startEvents[parent.traceId];
                            var childrenLen = parent.children.length;
                            if (parent.type == "CTS") {
                                // Parent STS can have many children that are initiated multiple client calls
                                // Parent CTS will have only one child which is the service call (Merge it to CTS itself)
                                // In all iterations except the initial* iteration Parent will be CTS
                                if (childrenLen == 1) {
                                    // The only child of CTS is a STS
                                    // Add children of STS to CTS
                                    var childSTS = parent.children[0];
                                    childSTS.end = endEvents[childSTS.traceId];
                                    parent.service = childSTS;
                                    parent.clientName = parent.instanceName;
                                    parent.instanceName = childSTS.instanceName;
                                    parent.children = startEvents[childSTS.traceId] || [];
                                    delete startEvents[childSTS.traceId];
                                }
                            }
                            parent.children.sort(function (a, b) {
                                return a.time - b.time;
                            });
                            childrenLen = parent.children.length;
                            for (var i = 0; i < childrenLen; i++) {
                                var childCTS = parent.children[i];
                                childCTS.end = endEvents[childCTS.traceId];
                                parents.push(childCTS);
                            }
                        }
                        this.isBuilt = true;
                        this.events = null;
                        console.log("TraceTree:");
                        console.log(origin);
                        if (origin.timeRange.end == 0) {
                            // This condition will satisfy if no end events are available
                            origin.timeRange.end = (new Date()).getTime();
                        }
                        return origin;
                    }
                }
            };
        }
        traceGroup.addEvent(event);
    }
    console.log("Trace groups:");
    console.log(traceGroups);
    return traceGroups;
}

function itrValidTraceGroups(traceGroups, callback) {
    for (var key in traceGroups) {
        if (!traceGroups.hasOwnProperty(key)) {
            continue;
        }
        var traceGroup = traceGroups[key];
        if (traceGroup.origin && validateOriginEvent(traceGroup.origin)) {
            callback(traceGroup);
        }
    }
}

function validateOriginEvent(event) {
    return event.instanceName && event.time && event.traceId && event.url && event.httpMethod && !event.parentId;
}
