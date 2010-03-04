% Marco Bettiol - 586580 - BATCH SIZE ESTIMATE
%
% CBT Full Test
%
% Estimate distributions obtained with CBT fixed different n
%
% Based on cbtsimpletest code

clear all;
close all;
clc;

% input
logn_max=15;
c=100000;

% generate the test batch size
x=1:logn_max;
testsizes=2.^x;

% resulting estimate distribution
% ED(log2(batch size), log2(estimate batch size));

ED=zeros(length(testsizes),length(testsizes)+40);

for i=1:length(testsizes)
    n=testsizes(i);
    disp(['Testing size :' int2str(n)]);
    if (n<2)
        error('BRA must start with a collision');
    end
    for ii=1:c
        %generate the batch
        nodes=rand(n,1); 
        nodes=[sort(nodes); 1];
        % CBT Estimate Simulation

        % true if we got a success in the last transmission
        lastwassuccess=false;
        %false to end CBT
        waitforconsecutive=true;

        imax=length(nodes);  %index of the first node in the batch
        imin=1;    % index of the first node in the batch
        xmin=0;    % starting interval [0,1/2) 
        xlen=1/2;  % we suppose a collision already occurred.
        l=1;       % current level in the tree

        while (waitforconsecutive)
            [e,imin,imax]=cbtsplit(nodes,imin,imax,xmin,xlen);
            if(e==1)
                xmin=xmin+xlen;
            elseif (e==0) 
                xmin=xmin+xlen;
                xlen=xlen/2;
                l=l+1;
            else
                xlen=xlen/2;
                l=l+1;
            end
            if (lastwassuccess==true && e==1)
                waitforconsecutive=false;
            end
            if(e==1)
                lastwassuccess=true;
            else
                lastwassuccess=false;
            end
        end
        ED(i,l)=ED(i,l)+1;
    end
end
