
angular.module('ASysMonApp').controller('CtrlAggregated', function($scope, $log, Rest, escapeHtml) {

    $('.button-segment').affix({
        offset: {
            top: 95
        }
    });

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


    $scope.expansionModel = {};
    $scope.rootLevel = 0;

    var nodesByFqn = {};

    function initFromResponse(data) {
//        $log.log('init from response');
        $scope.isStarted = data.isStarted;
        $scope.columnDefs = data.columnDefs.reverse();
        $scope.traces = data.traces;
        $scope.pickedTraces = $scope.traces; //TODO keep selection on 'refresh'

        nodesByFqn = {};
        initTraceNodes($scope.traces, 0, '');

//        $log.log('after init trace nodes');

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

    $scope.pickClass = function() {
        return $scope.isInPickMode ? 'btn-danger' : 'btn-default';
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

    function startsWith(s, prefix) {
        s = s || '';
        prefix = prefix || '';

        return s.indexOf(prefix) === 0;
    }

    $scope.expandAll = function() {
        function setExpanded(nodes) {
            if(nodes) {
                for(var i=0; i<nodes.length; i++) {
                    $scope.expansionModel[nodes[i].fqn] = true;
                    setExpanded(nodes[i].children);
                }
            }
        }

        setExpanded($scope.traces);
        renderTree();
    };

    $scope.collapseAll = function() {
        $scope.expansionModel = {};
        renderTree();
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
            return $scope.isExpanded(node) ? 'node-icon-expanded' : 'node-icon-collapsed';
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

        if(childrenDiv.hasClass('unrendered')) {
            childrenDiv.replaceWith(htmlForChildrenDiv(node, true));
            childrenDiv = dataRow.next();
            childrenDiv.find('.data-row.with-children').click(onClickNode);
        }

        childrenDiv.slideToggle(50, function() {
            $scope.$apply(function() {
                $scope.expansionModel[node.fqn] = !$scope.expansionModel[node.fqn];

                var nodeIconDiv = dataRow.children('.node-icon');
                if($scope.isExpanded(node)) {
                    nodeIconDiv.removeClass('node-icon-collapsed').addClass('node-icon-expanded');
                }
                else {
                    nodeIconDiv.addClass('node-icon-collapsed').removeClass('node-icon-expanded');
                }
            });
        });
    }

    $scope.$watch('traces === pickedTraces', function() {
        $('#unpick').attr('disabled', $scope.traces === $scope.pickedTraces);
    });
    function pickTreeNode(node) {
        $scope.$apply(function() {
            $scope.pickedTraces = [node];
            $scope.isInPickMode = false;
            $scope.rootLevel = node.level;
        });
        renderTree();
    }
    $scope.togglePickMode = function() {
        $scope.isInPickMode = ! $scope.isInPickMode;
    };
    $scope.unpick = function() {
        $scope.pickedTraces = $scope.traces;
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
        var hhttmmll = htmlForAllTrees();

        // it is an important performance optimization to explicitly unregister event listeners and remove old child
        //  elements from the DOM instead of implicitly removing them in the call to $(...).html(...) - the difference
        //  is seconds vs. minutes for large trees!
        $('.data-row.with-children').off();
        var myNode = document.getElementById("theTree");
        while (myNode.firstChild) {
            myNode.removeChild(myNode.firstChild);
        }

        $('#theTree').html(hhttmmll);

        $('.data-row.with-children').click(onClickNode);
    }

    function onClickNode() {
        var fqn = $(this).children('.fqn-holder').text();
        if($scope.isInPickMode) {
            pickTreeNode(nodesByFqn[fqn]);
        }
        else {
            toggleTreeNode($(this), nodesByFqn[fqn]);
        }
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

        var withChildrenClass = (curNode.children && curNode.children.length) ? ' with-children' : '';
        var result =
            '<div class="data-row data-row-' + (curNode.level - $scope.rootLevel) + withChildrenClass + ' ' + dataRowSubdued + '">' +
                '<div class="fqn-holder">' + curNode.fqn + '</div>' +
                '<div class="node-icon ' + $scope.nodeIconClass(curNode.fqn) + '">&nbsp;</div>' +
                dataCols +
                '<div class="node-text" style="margin-right: ' + $scope.totalDataWidth + 'px;">' + escapeHtml(curNode.name) + '</div>' +
                '</div>';

        result += htmlForChildrenDiv(curNode);

        return result;
    }

    function htmlForChildrenDiv(curNode, shouldRender) {
        if(! curNode.children || curNode.children.length === 0) {
            return '';
        }

        if(shouldRender || $scope.isExpanded(curNode)) {
            var result = '';
            result += '<div class="children" style="display: ' + $scope.expansionStyle(curNode) + ';">';
            angular.forEach(curNode.children, function(child) {
                result += htmlForTreeNode(child, $scope.rootLevel);
            });
            result += '</div>';
            return result;
        }
        else {
            return '<div class="children unrendered"></div>';
        }
    }
});

































