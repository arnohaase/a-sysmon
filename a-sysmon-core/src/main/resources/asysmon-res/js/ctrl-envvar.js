
angular.module('ASysMonApp').controller('CtrlEnvVar', function($scope, $log, Rest) {
    $('.btn').tooltip({
        container: 'body',
        html: true
    });

    var rootLevel = 0;
    var nodesByFqn = {};

    function initFromResponse(data) {
        $scope.envTree = data.envTree;
        nodesByFqn = {};
        initTreeNodes($scope.envTree, 0, '');

        $('#theTree').html(htmlForAllTrees());
        $('#theTree .data-row')
            .click(function() {
                var fqn = $(this).children('.fqn-holder').text();
                var childrenDiv = $(this).next();
                childrenDiv.slideToggle(50, function() {
                    $scope.$apply(function() {
//                        var nodeIconDiv = dataRow.children('.node-icon');
                        //TODO
//                        if($scope.expansionModel[node.fqn]) {
//                            nodeIconDiv.removeClass('node-icon-collapsed').addClass('node-icon-expanded');
//                        }
//                        else {
//                            nodeIconDiv.addClass('node-icon-collapsed').removeClass('node-icon-expanded');
//                        }
                    });
                });
            })
    }
    function initTreeNodes(nodes, level, prefix) {
        if(nodes) {
            for(var i=0; i<nodes.length; i++) {
                nodes[i].level = level;
                var fqn = prefix + '\n' + (nodes[i].id || nodes[i].name);
                nodes[i].fqn = fqn;
                nodesByFqn[fqn] = nodes[i];
                initTreeNodes(nodes[i].children, level+1, fqn);
            }
        }
    }


    Rest.call('getData', initFromResponse);

    function htmlForAllTrees() {
        var result = '';
        angular.forEach($scope.envTree, function(rootNode) {
            result +=
                '<div>' +
                    htmlForTreeNode(rootNode) +
                '</div>';
        });

        return result;
    }

    function htmlForTreeNode(curNode) {
        var result =
            '<div class="data-row data-row-' + (curNode.level - rootLevel) + '">' +
                '<div class="fqn-holder">' + curNode.fqn + '</div>' +
                '<div class="node-icon ' + nodeIconClass(curNode.fqn) + '">&nbsp;</div>' +
                (curNode.value ? ('<div style="float: right; width: 50%;">' + escapeHtml(curNode.value) + '</div>') : '') +
                '<div class="node-text" style="margin-right: 50%;">' + escapeHtml(curNode.name) + '</div>' +
                '</div>';

        if(curNode.children && curNode.children.length) {
            result += '<div class="children" style="display: none;">';
            angular.forEach(curNode.children, function(child) {
                result += htmlForTreeNode(child);
            });
            result += '</div>';
        }
        return result;
    }

    function nodeIconClass(node) {
        if(! node.fqn) {
            //TODO remove this heuristic
            node = nodesByFqn[node];
        }

        if(node.children && node.children.length) {
            return 'node-icon-collapsed';
//            return expansionModel[node.fqn] ? 'node-icon-expanded' : 'node-icon-collapsed';
        }
        return 'node-icon-empty';
    }

    var entityMap = {
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': '&quot;',
        "'": '&#39;',
        "/": '&#x2F;'
    };
    function escapeHtml(string) { //TODO extract to module
        return String(string).replace(/[&<>"'\/]/g, function (s) {
            return entityMap[s];
        });
    }
});

