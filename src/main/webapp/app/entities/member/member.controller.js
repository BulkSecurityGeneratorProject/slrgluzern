(function() {
    'use strict';

    angular
        .module('slrgApp')
        .controller('MemberController', MemberController);

    MemberController.$inject = ['$scope', '$state', 'Member', 'ParseLinks', 'AlertService'];

    function MemberController ($scope, $state, Member, ParseLinks, AlertService) {
        var vm = this;

        vm.members = [];
        vm.loadPage = loadPage;
        vm.page = 0;
        vm.links = {
            last: 0
        };
        vm.predicate = 'id';
        vm.reset = reset;
        vm.reverse = true;
        vm.filter = {
            text: '',
            aqua: false,
            skipper: false,
            boat: false,
            rescue: false,
            full: true
        };
        vm.download = download;

        loadAll();


        function loadAll () {
            Member.query({
                page: vm.page,
                size: 20,
                sort: sort()
            }, onSuccess, onError);
            function sort() {
                var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
                if (vm.predicate !== 'id') {
                    result.push('id');
                }
                return result;
            }
            function onSuccess(data, headers) {
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                for (var i = 0; i < data.length; i++) {
                    vm.members.push(data[i]);
                }
                console.log(vm.members);
            }
            function onError(error) {
                AlertService.error(error.data.message);
            }
        }

        function download () {
            var blob = new Blob([document.getElementById('exportable').innerHTML], {
                type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8"
            });
            saveAs(blob, "Report.xls");
        }

        function reset () {
            vm.page = 0;
            vm.members = [];
            loadAll();
        }

        function loadPage(page) {
            vm.page = page;
            loadAll();
        }
    }
})();
