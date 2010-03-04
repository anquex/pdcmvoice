% Marco Bettiol - 586580 - BATCH SIZE ESTIMATE

function [e,imin,imax]=cbtsplit(nodes,imin,imax,xmin,xlen)

%
% CBT BATCHSPLIT
%
% [e,imin,imax]=cbtsplit(nodes,imin,imax,xmin,xlen)
%
% Finds the nodes possibily enabled in the future conflicting set given the
% current enabled interval [xmin,xmin+xlen) and establish the event type
% for the current enabled interval
%
% Input:
%
% nodes : asc ordered vector of the nodes
% imin  : index of the first node that collided
% imax  : index of the first node in the sleeping set
% xmin  : lower bound of the new enabled interval
% xmin  : higher bound of the new enabled interval
%
% Output:
% e    : event obtained
% imin : new index of the first node that collided
% imax : new index of the first node in the sleeping set

xmax=xmin+xlen;

% idle slot happens when imin is greater than current max allowded value.
% Future set of nodes to analyze do not change
if (nodes(imin)>=xmax)
    e=0;
    return;
end

% this is always false by algorithm construction
% used to verify a trivial condition
%while (nodes(imin)<xmin)
%    imin=imin+1;
%end

% if event is a success
if (nodes(imin)<xmax && nodes(imin+1)>=xmax)
    e=1;
    imin=imin+1;
else
% if event is collision
% update the next enabled set
    e=2;
    while ((imax-1)~=0 && xmax<nodes(imax-1))
        imax=imax-1;
    end
end


    

