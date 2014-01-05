
angular.module('ASysMonApp').controller('CtrlAggregated', function($scope, $log, Rest) {

    function reinitTooltips() {
        $('.btn').tooltip({
            container: 'body',
            html: true
        });
    }

    reinitTooltips();
    $scope.$watch('isStarted', function() {
        $('.btn').tooltip('hide');
        setTimeout(reinitTooltips, 0);
    });


    $scope.expansionModel = {}; // bound to the DOM, used for initial rendering
    $scope.shadowExpansionModel = {}; // continually updated, kept separate to allow for jQuery animations
    $scope.rootLevel = 0;

    var nodesByFqn = {};

    function initFromResponse(data) {
        $scope.isStarted = data.isStarted;
        $scope.columnDefs = data.columnDefs.reverse();
        $scope.traces = data.traces;
        $scope.pickedTraces = $scope.traces; //TODO keep selection on 'refresh'

        nodesByFqn = {};
        initTraceNodes($scope.traces, 0, '');

        $scope.totalDataWidth = 0;
        for(var i=0; i<data.columnDefs.length; i++) {
            var cw = data.columnDefs[i].width.toLowerCase();
            if(cw === 'short') {
                $scope.totalDataWidth += 40;
            }
            if(cw === 'medium') {
                $scope.totalDataWidth += 60;
            }
            if(cw === 'long') {
                $scope.totalDataWidth += 100;
            }
        }

        $scope.expansionModel = angular.copy($scope.shadowExpansionModel);
        renderTree();
    }

    function initTraceNodes(nodes, level, prefix) {
        if(nodes) {
            for(var i=0; i<nodes.length; i++) {
                nodes[i].level = level;
                var fqn = prefix + '\n' + (nodes[i].id || nodes[i].name);
                nodes[i].fqn = fqn;
                nodesByFqn[fqn] = nodes[i];
                initTraceNodes(nodes[i].children, level+1, fqn);
            }
        }
    }

    function sendCommand(cmd) {
        Rest.call(cmd, initFromResponse);
    }

    $scope.refresh = function() {
        sendCommand('getData');
    };
    $scope.clear = function() {
        $scope.expansionModel = {};
        sendCommand('doClear');
    };
    $scope.start = function() {
        sendCommand('doStart');
    };
    $scope.stop = function() {
        sendCommand('doStop');
    };

    $scope.refresh();

    function revIdx(idx) {
        return $scope.columnDefs.length - idx - 1;
    }
    $scope.revIdx = function(idx) {
        return $scope.columnDefs.length - idx - 1;
    };

    var thousandsSeparator = 1234.5.toLocaleString().charAt(1);
    var decimalSeparator   = 1234.5.toLocaleString().charAt(5);

    $scope.formatNumber = function(number, numFracDigits) {
        var parts = number.toFixed(numFracDigits).toString().split('.');
        parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, thousandsSeparator);
        return parts.join(decimalSeparator);
    };

    $scope.formattedScalar = function(name, factor) {
        factor = factor || 1;
        var s = $scope.scalars && $scope.scalars[name];
        return s ? $scope.formatNumber(s.value*factor, s.numFracDigits) : '';
    };
    $scope.hasLoad = function() {
        return $scope.scalars && $scope.scalars['load-1-minute'];
    };
    $scope.load1 = function() {
        return $scope.formattedScalar('load-1-minute');
    };
    $scope.load5 = function() {
        return $scope.formattedScalar('load-5-minutes');
    };
    $scope.load15 = function() {
        return $scope.formattedScalar('load-15-minutes');
    };

    function startsWith(s, prefix) {
        s = s || '';
        prefix = prefix || '';

        return s.indexOf(prefix) === 0;
    }

    $scope.genericScalarNames = function() {


        var result = [];
        if($scope.scalars) {
            $.each($scope.scalars, function(n) {
                if(startsWith(n, 'load-')) {
                    return;
                }
                result.push(n);
            });
        }
        return result;
    };

    $scope.expandAll = function() {
        $('div.children').show(50);

        function setExpanded(nodes) {
            if(nodes) {
                for(var i=0; i<nodes.length; i++) {
                    $scope.shadowExpansionModel[nodes[i].fqn] = true;
                    setExpanded(nodes[i].children);
                }
            }
        }

        setExpanded($scope.traces);
    };

    $scope.collapseAll = function() {
        $('div.children').hide(50);
        $scope.shadowExpansionModel = {};
    };


    $scope.isPercentage = function(columnDef) {
        return columnDef.isPercentage;
    };
    $scope.isSubdued = function(node) {
        return !node.isSerial;
    };
    $scope.dataRowSubdued = function(node) {
        return node.isSerial ? '' : 'data-row-subdued';
    };
    $scope.progressWidthStyle = function(value) {
        return 'background-size: ' + (value + 2) + '% 100%';
    };
    $scope.colClass = function(idx) {
        return 'column-' + $scope.columnDefs[idx].width.toLowerCase();
    };

    $scope.nodeIconClass = function(node) {
        if(! node.fqn) {
            //TODO remove this heuristic
            node = nodesByFqn[node];
        }

        if(node.children && node.children.length) {
            return $scope.shadowExpansionModel[node.fqn] ? 'node-icon-expanded' : 'node-icon-collapsed';
        }
        return 'node-icon-empty';
    };
    $scope.expansionStyle = function(node) {
        return $scope.isExpanded(node) ? 'block' : 'none';
    };
    $scope.isExpanded = function(node) {
        return $scope.expansionModel[node.fqn];
    };
    $scope.clickTreeNode = function(event, node) {
        if($scope.isInPickMode) {
            pickTreeNode(node);
        }
        else {
            var clicked = $(event.target);
            var dataRow = clicked.parents('.data-row');
            toggleTreeNode(dataRow, node);
        }
    };

    function toggleTreeNode(dataRow, node) {
        var childrenDiv = dataRow.next();
        childrenDiv.slideToggle(50, function() {
            $scope.$apply(function() {
                $scope.shadowExpansionModel[node.fqn] = !$scope.shadowExpansionModel[node.fqn];

                var nodeIconDiv = dataRow.children('.node-icon');
                if($scope.shadowExpansionModel[node.fqn]) {
                    nodeIconDiv.removeClass('node-icon-collapsed').addClass('node-icon-expanded');
                }
                else {
                    nodeIconDiv.addClass('node-icon-collapsed').removeClass('node-icon-expanded');
                }
            });
        });
    }

    $scope.pickClass = function() {
        return $scope.isInPickMode ? 'btn-danger' : 'btn-default';
    };
    $scope.$watch('traces === pickedTraces', function() {
        $('#unpick').attr('disabled', $scope.traces === $scope.pickedTraces);
    });
    function pickTreeNode(node) {
        $scope.$apply(function() {
            $scope.pickedTraces = [node];
            $scope.isInPickMode = false;
            $scope.expansionModel = angular.copy($scope.shadowExpansionModel);
            $scope.rootLevel = node.level;
        });
        renderTree();
    }
    $scope.togglePickMode = function() {
        $scope.isInPickMode = ! $scope.isInPickMode;
    };
    $scope.unpick = function() {
        $scope.pickedTraces = $scope.traces;
        $scope.expansionModel = angular.copy($scope.shadowExpansionModel);
        $scope.rootLevel = 0;
        renderTree();
    };

    $scope.doExport = function() {
        function pad2(n) {
            var result = n.toString();
            while(result.length < 2) {
                result = '0' + result;
            }
            return result;
        }
        var now = new Date();
        var formattedNow = now.getFullYear() + '-' + pad2((now.getMonth()+1)) + '-' + pad2(now.getDate()) + '-' + pad2(now.getHours()) + '-' + pad2(now.getMinutes()) + '-' + pad2(now.getSeconds());

        var data = 'Name\tLevel';
        for(var i=0; i<$scope.columnDefs.length; i++) {
            data += '\t' + $scope.columnDefs[revIdx(i)].name;
        }

        function append(node) {
            var row = '\n';
            row += '                                                                                                     '.substring(0, 2*(node.level - $scope.rootLevel));
            row += node.name + '\t' + node.level;
            for(var i=0; i < $scope.columnDefs.length; i++) {
                row += '\t' + $scope.formatNumber(node.data[i], $scope.columnDefs[i].numFracDigits);
            }
            data += row;
            for(var j=0; j<(node.children || []).length; j++) {
                append(node.children[j]);
            }
        }
        for(var j=0; j<$scope.pickedTraces.length; j++) {
            append($scope.pickedTraces[j]);
        }

        var blob = new Blob([data], {type: "application/excel;charset=utf-8"});
        saveAs(blob, "asysmon-export-" + formattedNow + '.csv');
    };


    function renderTree() {
        $('#theTree').html(htmlForAllTrees()); // (or .child(...) or whatever)

        $('#theTree .data-row').click(function() {
            var fqn = $(this).children('.fqn-holder').text();
            if($scope.isInPickMode) {
                pickTreeNode(nodesByFqn[fqn]);
            }
            else {
                toggleTreeNode($(this), nodesByFqn[fqn]);
            }
        });
    }

    function htmlForAllTrees() {
        var htmlForTableHeader = (function(){
            var titles = '';

            angular.forEach($scope.columnDefs, function(curCol, colIdx) {
                titles += '<div class="' + $scope.colClass(colIdx) + '">' + curCol.name + '</div>';
            });

            return '' +
                '<div class="table-header">&nbsp;<div style="float:right;">' +
                titles +
                '</div></div>';
        }());

        var result = '';

        angular.forEach($scope.pickedTraces, function(rootNode) {
            result +=
                '<div>' +
                    htmlForTableHeader +
                    htmlForTreeNode(rootNode) +
                '</div>';
        });

        return result;
    }

    //TODO remove most function from $scope
    //TODO move 'formatNumber' to asysmon module
    function htmlForTreeNode(curNode) {
        var dataRowSubdued = curNode.isSerial ? '' : 'data-row-subdued';

        var dataCols = '';
        angular.forEach($scope.columnDefs, function(curCol, colIdx) {
            dataCols += '<div class="' + $scope.colClass(colIdx) + '">';

            var formattedValue = $scope.formatNumber(curNode.data[revIdx(colIdx)], $scope.columnDefs[colIdx].numFracDigits);

            if(curCol.isPercentage) {
                if(curNode.isSerial)
                    dataCols += '<div class="aprogress-background"><div class="aprogress-bar" style="' + $scope.progressWidthStyle(curNode.data[revIdx(colIdx)]) + '">' + formattedValue + '</div></div>';
                else
                    dataCols += '<div class="subdued-progress-background">' + formattedValue + '</div>';
            }
            else {
                dataCols += '<div>' + formattedValue + '</div>';
            }

            dataCols += '</div>';
        });

        var result =
            '<div class="data-row data-row-' + (curNode.level - $scope.rootLevel) + ' ' + dataRowSubdued + '">' +
                '<div class="fqn-holder">' + curNode.fqn + '</div>' +
                '<div class="node-icon ' + $scope.nodeIconClass(curNode.fqn) + '">&nbsp;</div>' + //TODO {{nodeIconClass(curNode.fqn)}}
                dataCols +
                '<div class="node-text" style="margin-right: ' + $scope.totalDataWidth + 'px;">' + escapeHtml(curNode.name) + '</div>' +
                '</div>';

        if(curNode.children && curNode.children.length) {
            result += '<div class="children" style="display: ' + $scope.expansionStyle(curNode) + ';">';
            angular.forEach(curNode.children, function(child) {
                result += htmlForTreeNode(child, $scope.rootLevel);
            });
            result += '</div>';
        }
        return result;
    }

    var entityMap = {
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': '&quot;',
        "'": '&#39;',
        "/": '&#x2F;'
    };

    function escapeHtml(string) {
        return String(string).replace(/[&<>"'\/]/g, function (s) {
            return entityMap[s];
        });
    }
});

