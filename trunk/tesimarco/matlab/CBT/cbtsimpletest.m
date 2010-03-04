% Marco Bettiol - 586580 - BATCH SIZE ESTIMATE
%
% CBT Simple Test
%
% This script implements a simulation of the estimate obtained 
% using CBT in a batch of size n.
%
% Nodes are initially uniformily picked-up in the interval [0,1)


clear all;
close all;
clc;

n=16; % batch size
disp(['Size :' int2str(n)]);
 
nodes=rand(n,1); 
% virtual node with value 1 to get easier search algorithm
% among the nodes

% asc sorting
nodes=[sort(nodes); 1];

if (n<2)
    error('BRA must start with a collision');
end
% CBT Simulation

% true if we got a success in the last transmission
lastwassuccess=false;
%false to end CBT
waitforconsecutive=true;

imax=length(nodes);  %index of the first node in the batch
imin=1;    % index of the first node in the batch
xmin=0;    % starting interval [0,1/2) 
xlen=1/2;  % we suppose a collision already occurred.

while (waitforconsecutive)
    [e,imin,imax]=cbtsplit(nodes,imin,imax,xmin,xlen);
    % update next analyzed interval
    
    if(e==1)
        xmin=xmin+xlen;
        %xlen=xlen;
    elseif (e==0) 
        xmin=xmin+xlen;
        xlen=xlen/2;
    else
        %xmin=xmin; 
        xlen=xlen/2;
    end
    if (lastwassuccess==true && e==1)
        disp(' ');
        disp('CBT completed :');
        disp(['Estimate :' num2str(1/xlen)]);
        disp(['Last node transmitting :' int2str(imin-1)]);
        waitforconsecutive=false;
    end
    if(e==1)
        lastwassuccess=true;
    else
        lastwassuccess=false;
    end
end

% DEBUG
% estimate is given by the first serie of descending differences in the 
% nodes ID's
dif=-1*ones(n-1,1); %negative init
for i=1:n-1
    dif(i)=nodes(i+1)-nodes(i);
end

nodes
dif